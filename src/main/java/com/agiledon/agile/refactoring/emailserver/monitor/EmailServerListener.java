package com.agiledon.agile.refactoring.emailserver.monitor;

import com.agiledon.agile.refactoring.emailserver.email.EmailAddress;
import com.agiledon.agile.refactoring.emailserver.email.EmailForwarder;
import com.agiledon.agile.refactoring.emailserver.email.EmailReceiver;
import com.agiledon.agile.refactoring.emailserver.email.EmailServerInfo;

/**
 * Created by IntelliJ IDEA.
 * User: twer
 * Date: 2/22/12
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailServerListener {
    private EmailForwarder forwarder;

    public void listen(EmailServerInfo serverInfo, EmailAddress[] addresses){
        EmailReceiver receiver = new EmailReceiver(serverInfo);
        if (receiver.received()){
            forwarder.forward(receiver.receiveMessages(), addresses);
        }

    }

    public void addForwarder(EmailForwarder forwarder){
        this.forwarder = forwarder;
    }
}
