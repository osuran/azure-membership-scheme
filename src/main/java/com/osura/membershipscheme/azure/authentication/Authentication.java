/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.osura.membershipscheme.azure.authentication;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.osura.membershipscheme.azure.exceptions.AzureMembershipSchemeException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Authentication {

    public AuthenticationResult getAuthToken(String authEndpoint, String armEndpoint, String username, String credentials, String tenantID, String clientID) throws AzureMembershipSchemeException {

        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        String url = null;

        try {
            service = Executors.newFixedThreadPool(1);
            url = authEndpoint + tenantID + "/oauth2/authorize";
            context = new AuthenticationContext(url, false, service);
            Future<AuthenticationResult> future = null;
            if (username == null) {
                ClientCredential cred = new ClientCredential(clientID, credentials);
                future = context.acquireToken(armEndpoint, cred, null);
            } else {
                future = context.acquireToken(armEndpoint, clientID, username, credentials, null);
            }
            result = future.get();
        } catch (Exception ex) {

            ex.printStackTrace();
            throw new AzureMembershipSchemeException("Could not connect to Azure API", ex);
        } finally {
            service.shutdown();
        }
        return result;
    }
}
