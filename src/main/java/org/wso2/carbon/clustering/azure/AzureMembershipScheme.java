/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.clustering.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.wso2.carbon.clustering.azure.authentication.Authentication;
import org.wso2.carbon.clustering.azure.domain.NetworkInterface;
import org.wso2.carbon.clustering.azure.domain.NetworkSecurityGroup;
import org.wso2.carbon.clustering.azure.exceptions.AzureMembershipSchemeException;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.wso2.carbon.utils.xml.StringUtils;

/**
 *
 * Azure membership scheme provides carbon cluster discovery on Microsoft Azure
 */
public class AzureMembershipScheme implements HazelcastMembershipScheme {

    private static final Log log = LogFactory.getLog(AzureMembershipScheme.class);
    private final Map<String, Parameter> parameters;
    protected final NetworkConfig nwConfig;
    private final List<ClusteringMessage> messageBuffer;
    private HazelcastInstance primaryHazelcastInstance;
    private HazelcastCarbonClusterImpl carbonCluster;
    // private boolean validationAuthority;

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
    public void init() throws ClusteringFault {
        try {
            log.info("Initializing Azure membership scheme...");
            nwConfig.getJoin().getMulticastConfig().setEnabled(false);
            nwConfig.getJoin().getAwsConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);

            String username = getConstant(Constants.azure_username, "", true);
            String credential = getConstant(Constants.azure_credential, "", false);
            String tenantId = getConstant(Constants.azure_tenantId, "", false);
            String clientId = getConstant(Constants.azure_clientId, "", false);
            String subscriptionId = getConstant(Constants.azure_subscriptionId, "", false);
            String resourceGroup = getConstant(Constants.azure_resourceGroup, "", false);
            String networkSecurityGroup = getConstant(Constants.azure_networkSecurityGroup, "default", false);
            String networkInterfaceTag = getConstant(Constants.azure_network_interface_tag, "default", false);
            boolean validationAuthority = Boolean.parseBoolean(getConstant(Constants.validationAuthorityValue, "false", true));

            if (networkInterfaceTag==null && networkSecurityGroup==null) {
                throw new ClusteringFault(String.format("both %s and %s parameters are empty. define at least one of them",
                        Constants.azure_networkSecurityGroup, Constants.azure_network_interface_tag));
            }

            Authentication auth = new Authentication();
            AuthenticationResult authToken = auth.getAuthToken(Constants.AUTHORIZATION_ENDPOINT, Constants.ARM_ENDPOINT,
                    null, credential, tenantId, clientId, validationAuthority);

            log.info(String.format("Azure clustering configuration: [autherization-endpont] %s , [arm-endpont] %s , [tenant-id] %s , [client-id] %s",
                    Constants.AUTHORIZATION_ENDPOINT, Constants.ARM_ENDPOINT, tenantId, clientId));

            List IPAddresses = new ArrayList(findVMIPaddresses(authToken, Constants.ARM_ENDPOINT, subscriptionId, resourceGroup,
                    networkSecurityGroup, networkInterfaceTag));
            for (Object IPAddress : IPAddresses) {
                nwConfig.getJoin().getTcpIpConfig().addMember(IPAddress.toString());
                log.info(String.format("Member added to cluster configuration: [IP Address] %s", IPAddress.toString()));
            }
            log.info("Azure membership scheme initialized successfully");
        } catch (Exception ex) {
            throw new ClusteringFault("Azure membership initialization failed", ex);
        }
    }

    protected List<String> findVMIPaddresses(AuthenticationResult result,
            String ARM_ENDPOINT, String subscriptionID, String resourceGroup,
            String networkSecurityGroup, String networkInterfaceTag) throws AzureMembershipSchemeException {

        List IPAddresses = new ArrayList();
        String url = null;
        InputStream instream = null;
        ObjectMapper objectMapper = new ObjectMapper();

        if (networkInterfaceTag==null) {
            //list NICs grouped in the specified network security group
            url = String.format(Constants.REST_API_AVAILABLE_NICs, ARM_ENDPOINT, subscriptionID, resourceGroup, networkSecurityGroup);
            instream = getAPIresponse(url, result);

            try {
                NetworkSecurityGroup nsg = objectMapper.readValue(instream, NetworkSecurityGroup.class);
                List ninames = nsg.getProperties().getNetworkInterfaceNames();

                for (Object niname : ninames) {
                    url = String.format(Constants.REST_API_NIC_INFO, ARM_ENDPOINT, subscriptionID, resourceGroup, niname);
                    instream = getAPIresponse(url, result);
                    NetworkInterface ni = objectMapper.readValue(instream, NetworkInterface.class);
                    IPAddresses.add(ni.getProperties().getIPAddress());
                }
            } catch (IOException ex) {
                throw new AzureMembershipSchemeException("Could not find VM IP addresses", ex);
            }
        } else if (networkSecurityGroup==null) { //List NICs according to the tags
            try {
                url = String.format(Constants.REST_API_TAG, ARM_ENDPOINT, subscriptionID, networkInterfaceTag);
                //String body= inputStreamToString(getAPIresponse(url, result));
                JSONObject root1 = new JSONObject(inputStreamToString(getAPIresponse(url, result)));
                JSONArray values = root1.getJSONArray("value");
                List ninames = new ArrayList();
                for (int i = 0; i < values.length(); i++) {
                    JSONObject firstelement = values.getJSONObject(i);
                    Object name = firstelement.get("name");
                    ninames.add(name);
                }
                for (Object niname : ninames) {
                    url = String.format(Constants.REST_API_NIC_INFO, ARM_ENDPOINT, subscriptionID, resourceGroup, niname);
                    instream = getAPIresponse(url, result);
                    NetworkInterface ni = objectMapper.readValue(instream, NetworkInterface.class);
                    IPAddresses.add(ni.getProperties().getIPAddress());
                }
            } catch (IOException ex) {
                throw new AzureMembershipSchemeException("Could not find VM IP addresses", ex);
            }
        } else {
            throw new AzureMembershipSchemeException("EITHER networkSecurityGroup OR networkInterfaceTag "
                    + "must be chosen as the grouping method; not both of them");
        }
        return IPAddresses;
    }

    public InputStream getAPIresponse(String url, AuthenticationResult result) throws AzureMembershipSchemeException {

        InputStream instream = null;
        try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams
                    .setConnectionTimeout(httpClient.getParams(), 10000);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Authorization", "Bearer " + result.getAccessToken());
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            instream = entity.getContent();

        } catch (Exception ex) {
            throw new AzureMembershipSchemeException("Could not connect to Azure API", ex);
        }
        return instream;
    }

    public void joinGroup() throws ClusteringFault {
        primaryHazelcastInstance.getCluster().addMembershipListener(new AzureMembershipSchemeListener());
    }

    private Parameter getParameter(String name) {
        return parameters.get(name);
    }

    protected String getConstant(String constant, String defaultValue, boolean isOptional) throws ClusteringFault {
        String param = System.getenv(constant);
        Parameter parameter;
        if (StringUtils.isEmpty(param)) {
            parameter = getParameter(constant);
            if (parameter == null) {
                param = defaultValue;
                if (StringUtils.isEmpty(param) && !isOptional) {   //should leave defaultvalue blank if the value is mandatory
                    throw new ClusteringFault(String.format("Azure %s parameter not found", constant));
                } else {
                    param = null;
                }
            } else {
                return parameter.getValue().toString();
            }
        }
        return param;
    }

    public String inputStreamToString(InputStream instream) throws IOException {
        String body = null;
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(instream), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        instream.close();
        body = sb.toString();

        return body;
    }

    private class AzureMembershipSchemeListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            // Send all cluster messages
            carbonCluster.memberAdded(member);
            log.info(String.format("Member joined [%s] : %s", member.getUuid(), member.getSocketAddress().toString()));
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
            log.info(String.format("Member left [%s] : %s", member.getUuid(), member.getSocketAddress().toString()));
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Member attribute changed [%s] %s", memberAttributeEvent.getKey(), memberAttributeEvent.getValue()));
            }
        }
    }
}
