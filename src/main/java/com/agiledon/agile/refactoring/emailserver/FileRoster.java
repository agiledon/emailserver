package com.agiledon.agile.refactoring.emailserver;

import javax.mail.Address;

/**
 * Created by IntelliJ IDEA.
 * User: twer
 * Date: 2/19/12
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileRoster implements Roster {
    public FileRoster(String fileName) {
    }

    public boolean constainsOneOf(Address[] from) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Address[] getAddresses() {
        return new Address[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
