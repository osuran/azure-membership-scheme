/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author Osura
 */
public class Constants {
    
    final static String AUTHORIZATION_ENDPOINT = "https://login.microsoftonline.com/";
    final static String ARM_ENDPOINT = "https://management.azure.com/";
    static String username = null;
    static String credential = "";
    static String tenantId = "";
    static String clientId = "";
    static String subscriptionId = "";
    static String resourceGroup = "ASCluster";
    static String NSG = "ASNSG";
    
    
}
