/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.osura.membershipscheme;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Authentication {
  
    public AuthenticationResult getAuthToken(String authEndpoint, String armEndpoint, String username, String credentials, String tenantID, String clientID){
        
    AuthenticationContext context = null;
    AuthenticationResult result = null;
    ExecutorService service = null;
    String url=null;
    
    try {
            service = Executors.newFixedThreadPool(1);
            url = authEndpoint + tenantID + "/oauth2/authorize";
            context = new AuthenticationContext(url,false,service);
            Future<AuthenticationResult> future = null;
            if(username == null) 
            {
	            ClientCredential cred = new ClientCredential(clientID, credentials);
	            future = context.acquireToken(armEndpoint, cred, null);
	    } 
            else 
            {
            	future = context.acquireToken(armEndpoint, clientID,username, credentials, null);
            }
            result = future.get();
        } catch (Exception ex) {
        	System.out.println("Exception occurred: "+ex.getMessage());
	        ex.printStackTrace();
            System.exit(1);
        } finally {
            service.shutdown();
        }
    return result;
    }
}
