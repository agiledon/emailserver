package com.agiledon.agile.refactoring.emailserver;

/**
 * Created by IntelliJ IDEA.
 * User: twer
 * Date: 2/19/12
 * Time: 1:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class HostInformation {
    public String pop3Host;
    public String pop3User;
    public String pop3Password;
    public String smtpHost;
    public String smtpUser;
    public String smtpPassword;

    public HostInformation(String[] inputParas){
        pop3Host = inputParas[0];
        smtpHost = inputParas[1];
        pop3User = inputParas[2];
        pop3Password = inputParas[3];
        smtpUser = inputParas[4];
        smtpPassword = inputParas[5];
    }
}
