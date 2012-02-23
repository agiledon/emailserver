package com.agiledon.agile.refactoring.emailserver;

/**
 * Created by IntelliJ IDEA.
 * User: twer
 * Date: 2/19/12
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class RosterReader {
    private static boolean isFailure = true;
    public static Roster readRoster() {
        Roster roster = null;
        try {
            roster = new FileRoster("roster.txt");
                   isFailure = false;
        } catch (Exception e) {
            System.err.println("unable to open roster.txt");
        }
        return roster;
    }

    public static boolean isFailure(){
        return isFailure;
    }
}
