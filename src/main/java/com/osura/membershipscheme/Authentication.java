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
//import org.json.JSONArray;
//import org.json.JSONObject;

public class Authentication {
    
    private final static String AUTHORIZATION_ENDPOINT = Constants.AUTHORIZATION_ENDPOINT;
    private final static String ARM_ENDPOINT = Constants.ARM_ENDPOINT;
    static String username = null;
    static String credential = Constants.credential;
    static String tenantId = Constants.tenantId;
    static String clientId = Constants.clientId;
    static String subscriptionId = Constants.subscriptionId;
    static String resourceGroup = Constants.resourceGroup;
    static String NSG = Constants.NSG;
    static AuthenticationContext context = null;
    static  AuthenticationResult result = null;
    static  ExecutorService service = null;
    String url=null;
    
    public AuthenticationResult getAuthToken(){
    
    try {
            service = Executors.newFixedThreadPool(1);
            url = AUTHORIZATION_ENDPOINT + tenantId + "/oauth2/authorize";
            context = new AuthenticationContext(url,false,service);
            Future<AuthenticationResult> future = null;
            if(username == null) 
            {
                   // System.out.println("1");
	            ClientCredential cred = new ClientCredential(clientId, credential);
                  //  System.out.println("2");
	            future = context.acquireToken(ARM_ENDPOINT, cred, null);
                 //   System.out.println(future);
	    } 
            else 
            {
            	future = context.acquireToken(ARM_ENDPOINT, clientId,username, credential, null);
            }
            result = future.get();
          //  System.out.println("4");
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
