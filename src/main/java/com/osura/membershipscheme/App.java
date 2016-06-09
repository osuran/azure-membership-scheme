package com.osura.membershipscheme;

import java.util.HashMap;
import java.util.Map;
import org.apache.axis2.description.Parameter;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        Map param= new HashMap();
        param.put("AURORIZATION_ENDPOINT",new Parameter("AURORIZATION_ENDPOINT", "https://login.microsoftonline.com/"));
        param.put("ARM_ENDPOINT",new Parameter("ARM_ENDPOINT","https://management.azure.com/"));
        param.put("clientId",new Parameter("clientID",""));
        param.put("tenantId",new Parameter("tenantID",""));
        param.put("subscriptionId",new Parameter("subscriptionId",""));
        param.put("credential",new Parameter("credential",""));
        param.put("resourceGroup",new Parameter("resourceGroup","ASCluster"));
        param.put("NSG",new Parameter("NSG","ASNSG"));
        
        AzureMembershipScheme az=new AzureMembershipScheme(param);
        az.init();
    }
        
}
