package com.agiledon.agile.refactoring.email;

import javax.mail.Address;

/**
 * Created by IntelliJ IDEA.
 * User: twer
 * Date: 2/19/12
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Roster {
    boolean constainsOneOf(Address[] from);

    Address[] getAddresses();
}
