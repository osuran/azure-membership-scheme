/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.osura.membershipscheme.azure.exceptions;

/**
 *
 * @author Osura
 */
public class AzureMembershipSchemeException extends Exception {

    public AzureMembershipSchemeException(String message) {
        super(message);
    }

    public AzureMembershipSchemeException(String message, Throwable cause) {
        super(message, cause);
    }
}
