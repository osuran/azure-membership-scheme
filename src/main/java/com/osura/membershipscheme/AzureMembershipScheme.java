/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.osura.membershipscheme;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.microsoft.aad.adal4j.AuthenticationResult;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;

/**
 *
 * @author Osura
 */
public class AzureMembershipScheme implements HazelcastMembershipScheme {
    
    private static final Log log = LogFactory.getLog(AzureMembershipScheme.class);

    private final Map<String, Parameter> parameters;
    protected final NetworkConfig nwConfig;
    private final List<ClusteringMessage> messageBuffer;
    private HazelcastInstance primaryHazelcastInstance;
    private HazelcastCarbonClusterImpl carbonCluster;
  //  private boolean skipMasterSSLVerification;

    public AzureMembershipScheme(Map<String, Parameter> parameters,
                                      String primaryDomain,
                                      Config config,
                                      HazelcastInstance primaryHazelcastInstance,
                                      List<ClusteringMessage> messageBuffer) {
        this.parameters = parameters;
        this.primaryHazelcastInstance = primaryHazelcastInstance;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
    }
    
//    public AzureMembershipScheme()
//    {
//        this.parameters = null;
//        this.primaryHazelcastInstance = null;
//        this.messageBuffer = null;
//        this.nwConfig = null;
//    }
    
    @Override
    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {
    }

    @Override
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }
    
    @Override
    public void init() throws ClusteringFault
    {
        

     
    String AUTHORIZATION_ENDPOINT = Constants.AUTHORIZATION_ENDPOINT;
    String ARM_ENDPOINT = Constants.ARM_ENDPOINT;
    String username = Constants.username;
    String credential = Constants.credential;
    String tenantId = Constants.tenantId;
    String clientId = Constants.clientId;
    String subscriptionId = Constants.subscriptionId;
    String resourceGroup=Constants.resourceGroup;
    String networkSecurityGroup = Constants.NSG;
      
        try {
            System.out.println("Initializing kubernetes membership scheme...");
            log.info("Initializing kubernetes membership scheme...");

            nwConfig.getJoin().getMulticastConfig().setEnabled(false);
            nwConfig.getJoin().getAwsConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
         //   System.out.println("1");
            
            
            Authentication auth= new Authentication();
            AuthenticationResult authToken= auth.getAuthToken();
            System.out.println(String.format("Azure clustering configuration: [autherization-endpont] %s [arm-endpont] %s [tenant-id] %s [client-id] %s",
                    AUTHORIZATION_ENDPOINT, ARM_ENDPOINT, tenantId, clientId));
            log.info(String.format("Azure clustering configuration: [autherization-endpont] %s [arm-endpont] %s [tenant-id] %s [client-idl-verification] %s",
                    AUTHORIZATION_ENDPOINT, ARM_ENDPOINT, tenantId, clientId));

            
           List IPAdresses = new ArrayList(findVMIPaddresses(authToken, ARM_ENDPOINT, subscriptionId,resourceGroup ,networkSecurityGroup));
                for(int i=0; i<IPAdresses.size();i++) {
                    tcpIpConfig.addMember(IPAdresses.get(i).toString());
                    log.info("Member added to cluster configuration: [container-ip] " + IPAdresses.get(i).toString());
                }
        for(int i=0; i<IPAdresses.size();i++) {
            System.out.println(IPAdresses.get(i).toString());
        }
}
        
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
            log.error(ex);
            throw new ClusteringFault("Kubernetes membership initialization failed", ex);
            
        }
        
    }
    
    protected List<String> findVMIPaddresses(AuthenticationResult result, String ARM_ENDPOINT, String subscriptionID, String resourceGroup, String networkSecurityGroup)
    {
        List IPAddresses= new ArrayList();
        //list NICs grouped in the specified network security group
        String url=String.format("%ssubscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/networkSecurityGroups/%s?api-version=2016-03-30",ARM_ENDPOINT,subscriptionID,resourceGroup,networkSecurityGroup);
        String body = getAPIresponse(url,result);

         JSONObject root1 = new JSONObject(body); 
         JSONObject propertiesOb =  root1.getJSONObject("properties");
         JSONArray NIArray = propertiesOb.getJSONArray("networkInterfaces");
      
        
        int len=NIArray.length(); //no of NICs found in the network security group
        JSONObject[] NIC= new JSONObject[len];
        
        for(int i=0; i<len; i++)
        { 
          NIC[i] = NIArray.getJSONObject(i);
        
        }
        
                String[] NICname= new String[len]; //NIC names 

            System.out.println("Printing the names of NICs");
            
            StringTokenizer[] st = new StringTokenizer[len];
            
            for (int i=0; i<len; i++)
            {
                st[i]= new StringTokenizer(NIC[i].toString(),"/");
                while(st[i].hasMoreTokens()) {
                NICname[i]=st[i].nextToken();
                }
                NICname[i]=NICname[i].substring(0,NICname[i].length()-2);
            System.out.println(NICname[i]);
            }
        
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //getting info on each NIC

            JSONObject root2= null;
            JSONObject propertiesOb2 =  null;
            JSONArray IPConfigArray = null;
            JSONObject firstIPConfig=null;
            JSONObject propertiesIPConfig= null;
            
           // String[] privateIPAddress=new String[len];
            System.out.println("Printing the IP addresses of NICs");
            for(int i=0; i<len; i++)
            {
                url = String.format("%ssubscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/networkInterfaces/%s?api-version=2016-03-30",ARM_ENDPOINT,subscriptionID,resourceGroup,NICname[i]);
                body=getAPIresponse(url,result);

                root2= new JSONObject(body);
                propertiesOb2 =  root2.getJSONObject("properties");
                IPConfigArray = propertiesOb2.getJSONArray("ipConfigurations");
                firstIPConfig=IPConfigArray.getJSONObject(0);
                propertiesIPConfig=firstIPConfig.getJSONObject("properties");
                IPAddresses.add(propertiesIPConfig.getString("privateIPAddress"));
                //privateIPAddress[i]=propertiesIPConfig.getString("privateIPAddress");
               // System.out.println(IPAddresses.get(i));
                
            }
        return IPAddresses;
    }
        public String getAPIresponse(String url,AuthenticationResult result)
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
            } 
        catch (Exception ex) {
          //  System.out.println("osura1");
            System.out.println(ex.toString());
            System.exit(1);
            }
        return body;
    
        }

     public void joinGroup() throws ClusteringFault 
     {
        primaryHazelcastInstance.getCluster().addMembershipListener(new KubernetesMembershipSchemeListener());
    }
    
     private class KubernetesMembershipSchemeListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // Send all cluster messages
            carbonCluster.memberAdded(member);
            log.info("Member joined [" + member.getUuid() + "]: " + member.getSocketAddress().toString());
            // Wait for sometime for the member to completely join before replaying messages
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            carbonCluster.memberRemoved(member);
            log.info("Member left [" + member.getUuid() + "]: " + member.getSocketAddress().toString());
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            if (log.isDebugEnabled()) {
                log.debug("Member attribute changed: [" + memberAttributeEvent.getKey() + "] " +
                        memberAttributeEvent.getValue());
            }
        }
    }
     
     
    
}
