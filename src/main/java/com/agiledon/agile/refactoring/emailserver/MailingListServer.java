package com.agiledon.agile.refactoring.emailserver;

import com.agiledon.agile.refactoring.emailserver.exceptions.ReadFolderError;

import java.io.IOException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

public class MailingListServer {
    public static final String SUBJECT_MARKER = "[list]";
    public static final String LOOP_HEADER = "X-LOOP";

    public static void main(String[] args) {
        if (args.length != 8) {
            System.err.println("Usage: java MailingList <popHost> " +
                    "<smtpHost> <pop3user> <pop3password> <smtpuser> <smtppassword> <listname> " +
                    "<relayinterval");
            return;
        }

        HostInformation host = new HostInformation(args);
        String listAddress = args[6];
        int interval = new Integer(args[7]).intValue();
        Roster roster = null;
        try {
            roster = new FileRoster("roster.txt");
        } catch (Exception e) {

        }

        try {
            do {
                try {
                    Properties properties = System.getProperties();
                    Session session = Session.getDefaultInstance(properties, null);

                    Store store = session.getStore("pop3");
                    store.connect(host.pop3Host, -1, host.pop3User, host.pop3Password);

                    Folder folder = getInboxFolder(store);
                    folder.open(FOLDER.READ_WRITE);

                    try {
                        if (folder.getMessageCount() != 0) {

                            Message[] messages = folder.getMessages();
                            FetchProfile fp = new FetchProfile();
                            fp.add(FetchProfile.Item.ENVELOPE);
                            fp.add(FetchProfile.Item.FLAGS);
                            fp.add("X-Mailer");
                            folder.fetch(messages, fp);

                            for (int i = 0; i < messages.length; i++) {
                                Message message = messages[i];
                                if (message.getFlags().contains(Flags.Flag.DELETED)) continue;
                                System.out.println("message received: " + message.getSubject());
                                if (!roster.constainsOneOf(message.getFrom())) continue;

                                MimeMessage forward = assemblForwardMessage(listAddress, roster, session, message);

                                sendMessage(host, roster, message, forward);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("message handling error");
                        e.printStackTrace(System.err);
                    } finally {
                        folder.close(true);
                        store.close();
                    }
                } catch (Exception e) {
                    System.err.println(e);
                    System.err.println("(Retrying mail check");
                }
                System.err.print(".");
                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException e) {

                }
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Folder getInboxFolder(Store store) throws MessagingException {
        Folder defaultFolder = store.getDefaultFolder();
        if (defaultFolder == null) {
            throw new ReadFolderError("Default Folder is null");
        }
        Folder folder = defaultFolder.getFolder("INBOX");
        if (folder == null) {
            throw new ReadFolderError("Inbox folder is null");
        }
        return folder;
    }

    private static void sendMessage(HostInformation host, Roster roster, Message message, MimeMessage forward) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", host.smtpHost);
        Session smtpSession = Session.getDefaultInstance(props, null);
        Transport transport = smtpSession.getTransport("smtp");
        transport.connect(host.smtpHost, host.smtpUser, host.smtpPassword);
        transport.sendMessage(forward, roster.getAddresses());
        message.setFlag(Flags.Flag.DELETED, true);
    }

    private static MimeMessage assemblForwardMessage(String listAddress, Roster roster, Session session, Message message) throws MessagingException, IOException {
        MimeMessage forward = new MimeMessage(session);
        Address[] fromAddress = message.getFrom();
        InternetAddress from = null;
        if (fromAddress != null && fromAddress.length > 0) {
            from = new InternetAddress(fromAddress[0].toString());
        }
        forward.setFrom(from);
        forward.setReplyTo(new Address[]{
                new InternetAddress(listAddress)
        });
        forward.addRecipients(Message.RecipientType.BCC, roster.getAddresses());
        String subject = message.getSubject();
        if (-1 == message.getSubject().indexOf(SUBJECT_MARKER)) {
            subject = SUBJECT_MARKER + " " + message.getSubject();
        }
        forward.setSubject(subject);
        forward.setSentDate(message.getSentDate());
        forward.addHeader(LOOP_HEADER, listAddress);
        Object content = message.getContent();
        if (content instanceof Multipart) {
            forward.setContent((Multipart) content);
        } else {
            forward.setText((String) content);
        }
        return forward;
    }

}