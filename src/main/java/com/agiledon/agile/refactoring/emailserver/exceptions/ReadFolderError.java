package com.agiledon.agile.refactoring.emailserver.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: twer
 * Date: 2/26/12
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReadFolderError extends RuntimeException {
    public ReadFolderError(String message) {
        super(message);
    }
}
