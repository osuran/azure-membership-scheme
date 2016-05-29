package example;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.message.AbstractHttpMessage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONArray;

import org.json.JSONObject;

public class ApplicationAuthExample {
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

    public static void main(String[] args) throws Exception {

         ApplicationAuthExample app=new ApplicationAuthExample();
         String body=null;
         String url=null;
        // use adal to Authenticate


	try {
            service = Executors.newFixedThreadPool(1);
            url = AUTHORIZATION_ENDPOINT + tenantId + "/oauth2/authorize";
            context = new AuthenticationContext(url,false,service);
            Future<AuthenticationResult> future = null;
            if(username == null) 
            {
	            ClientCredential cred = new ClientCredential(clientId, credential);
	            future = context.acquireToken(ARM_ENDPOINT, cred, null);
	    } 
            else 
            {
            	future = context.acquireToken(ARM_ENDPOINT, clientId,username, credential, null);
            }
            result = future.get();
        } catch (Exception ex) {
        	System.out.println("Exception occurred: "+ex.getMessage());
	        ex.printStackTrace();
            System.exit(1);
        } finally {
            service.shutdown();
        }
        

            
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///Getting information on ASNSG where VMs are added
            url = String.format("%ssubscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/networkSecurityGroups/%s?api-version=2016-03-30",ARM_ENDPOINT,subscriptionId,resourceGroup,NSG);
           
            body=app.getAPIresponse(url);
                
             JSONObject root1 = new JSONObject(body); 
             JSONObject propertiesOb =  root1.getJSONObject("properties");
             JSONArray NIArray = propertiesOb.getJSONArray("networkInterfaces");
      
        
        int len=NIArray.length();
        
        JSONObject[] NIC= new JSONObject[len];
        
        for(int i=0; i<len; i++)
        { 
          NIC[i] = NIArray.getJSONObject(i);
        
        }
        
        String[] NICname= new String[len];

            System.out.println("Printing the names of NICs");
        
            StringTokenizer tokenNI0=new StringTokenizer(NIC[0].toString(),"/");
            while(tokenNI0.hasMoreTokens()) {
                NICname[0]=tokenNI0.nextToken();
            }
            NICname[0]=NICname[0].substring(0,NICname[0].length()-2);
            System.out.println(NICname[0]);
            
             StringTokenizer tokenNI1=new StringTokenizer(NIC[1].toString(),"/");
            while(tokenNI1.hasMoreTokens()) {
                NICname[1]=tokenNI1.nextToken();
            }
            NICname[1]=NICname[1].substring(0,NICname[1].length()-2);
            System.out.println(NICname[1]);
            
             StringTokenizer tokenNI2=new StringTokenizer(NIC[2].toString(),"/");
            while(tokenNI2.hasMoreTokens()) {
                NICname[2]=tokenNI2.nextToken();
            }
            NICname[2]=NICname[2].substring(0,NICname[2].length()-2);
            System.out.println(NICname[2]);
            
             StringTokenizer tokenNI3=new StringTokenizer(NIC[3].toString(),"/");
            while(tokenNI3.hasMoreTokens()) {
                NICname[3]=tokenNI3.nextToken();
            }
            NICname[3]=NICname[3].substring(0,NICname[3].length()-2);
            System.out.println(NICname[3]);
            
             StringTokenizer tokenNI4=new StringTokenizer(NIC[4].toString(),"/");
            while(tokenNI4.hasMoreTokens()) {
                NICname[4]=tokenNI4.nextToken();
            }
            NICname[4]=NICname[4].substring(0,NICname[4].length()-2);
            System.out.println(NICname[4]);
            
               StringTokenizer tokenNI5=new StringTokenizer(NIC[5].toString(),"/");
            while(tokenNI5.hasMoreTokens()) {
                NICname[5]=tokenNI5.nextToken();
            }
            NICname[5]=NICname[5].substring(0,NICname[5].length()-2);
            System.out.println(NICname[5]);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //getting info on each NIC

            JSONObject root2= null;
            JSONObject propertiesOb2 =  null;
            JSONArray IPConfigArray = null;
            JSONObject firstIPConfig=null;
            JSONObject propertiesIPConfig= null;
        
            
            String[] privateIPAddress=new String[len];
            System.out.println("Printing the IP addresses of NICs");
            for(int i=0; i<len; i++)
            {
                url = String.format("%ssubscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/networkInterfaces/%s?api-version=2016-03-30",ARM_ENDPOINT,subscriptionId,resourceGroup,NICname[i]);
                body=app.getAPIresponse(url);

                root2= new JSONObject(body);
                propertiesOb2 =  root2.getJSONObject("properties");
                IPConfigArray = propertiesOb2.getJSONArray("ipConfigurations");
                firstIPConfig=IPConfigArray.getJSONObject(0);
                propertiesIPConfig=firstIPConfig.getJSONObject("properties");
                privateIPAddress[i]=propertiesIPConfig.getString("privateIPAddress");
                System.out.println(privateIPAddress[i]);
                
            }
           
        }
    
    public String getAPIresponse(String url)
    {
       // url = ARM_ENDPOINT + "subscriptions/" + subscriptionId + "/resourceGroups/ASCluster/providers/Microsoft.Network/networkSecurityGroups/ASNSG?api-version=2016-03-30";
      
        String body = null;
        try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams
                    .setConnectionTimeout(httpClient.getParams(), 10000);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Authorization", "Bearer " + result.getAccessToken());
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(instream), 1000);
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                sb.append(line);
            }
            instream.close();
           body=sb.toString();
           
//            BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("E:/demo.txt")));
//            bwr.write(sb.toString());
//            bwr.flush();
//            bwr.close();
        } catch (Exception ex) {
          //  System.out.println("osura1");
            System.out.println(ex.toString());
            System.exit(1);
        }
        return body;
    
    }
         
}

