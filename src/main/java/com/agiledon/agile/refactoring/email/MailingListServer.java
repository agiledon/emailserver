package com.agiledon.agile.refactoring.email;

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

        Roster roster = RosterReader.readRoster();
        if (RosterReader.isFailure()) return;

        try {
            do {
                try {

                    Properties properties = System.getProperties();
                    Session session = Session.getDefaultInstance(properties, null);
                    Store store = session.getStore("pop3");
                    store.connect(host.pop3Host, -1, host.pop3User, host.pop3Password);
                    Folder defaultFolder = store.getDefaultFolder();
                    if (defaultFolder == null) {
                        return;
                    }
                    Folder folder = defaultFolder.getFolder("INBOX");

                    if (folder == null) {
                        return;
                    }
                    folder.open(FOLDER.READ_WRITE);
                    try {
                        if (folder.getMessageCount() != 0) {
                            Message[] messages = null;

                            messages = receiveMessages(folder);

                            for (int i = 0; i < messages.length; i++) {
                                Properties props = new Properties();
                                Session smtpSession = Session.getDefaultInstance(props, null);
                                Transport transport = smtpSession.getTransport("smtp");


                                Message message = messages[i];

                                if (isDeletedMessage(message)) continue;

                                System.out.println("message received: " + message.getSubject());

                                if (!roster.constainsOneOf(message.getFrom())) continue;

                                sendMessage(host, listAddress, roster, session, props, transport, message);
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

    private static Message[] receiveMessages(Folder folder) throws MessagingException {
        Message[] messages;
        messages = folder.getMessages();
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add("X-Mailer");
        folder.fetch(messages, fp);
        return messages;
    }

    private static void sendMessage(HostInformation host, String listAddress, Roster roster, Session session, Properties props, Transport transport, Message message) throws MessagingException, IOException {
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

        props.put("mail.smtp.host", host.smtpHost);


        transport.connect(host.smtpHost, host.smtpUser, host.smtpPassword);
        transport.sendMessage(forward, roster.getAddresses());
        message.setFlag(Flags.Flag.DELETED, true);
    }

    private static boolean isDeletedMessage(Message message) throws MessagingException {
        return message.getFlags().contains(Flags.Flag.DELETED);
    }

}
