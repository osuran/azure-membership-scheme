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
import org.wso2.carbon.clustering.azure.authentication.Authentication;
import org.wso2.carbon.clustering.azure.domain.NetworkInterface;
import org.wso2.carbon.clustering.azure.domain.NetworkSecurityGroup;
import org.wso2.carbon.clustering.azure.exceptions.AzureMembershipSchemeException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.utils.xml.StringUtils;

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
    private boolean validationAuthority;

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

            String AUTHORIZATION_ENDPOINT = System.getenv(Constants.AUTHORIZATION_ENDPOINT);
            String ARM_ENDPOINT = System.getenv(Constants.ARM_ENDPOINT);
            String username = System.getenv(Constants.username);
            String credential = System.getenv(Constants.credential);
            String tenantId = System.getenv(Constants.tenantId);
            String clientId = System.getenv(Constants.clientId);
            String subscriptionId = System.getenv(Constants.subscriptionId);
            String resourceGroup = System.getenv(Constants.resourceGroup);
            String networkSecurityGroup = System.getenv(Constants.NSG);
            String validationAuthorityValue = System.getenv(Constants.validationAuthorityValue);


            if (StringUtils.isEmpty(AUTHORIZATION_ENDPOINT)) {

                AUTHORIZATION_ENDPOINT = getParameterValue(Constants.AUTHORIZATION_ENDPOINT, "https://login.microsoftonline.com/");
            }

            if (StringUtils.isEmpty(ARM_ENDPOINT)) {
                ARM_ENDPOINT = getParameterValue(Constants.ARM_ENDPOINT, "//https://management.azure.com/");
            }

            if (StringUtils.isEmpty(validationAuthorityValue)) {
                validationAuthorityValue = getParameterValue(Constants.validationAuthorityValue, "false");

            }

            validationAuthority = Boolean.parseBoolean(validationAuthorityValue);

            if (StringUtils.isEmpty(username)) {
                username = getParameterValue(Constants.username, "username");
                if (username.equals("username")) {
                    username = null;
                }
            }

            if (StringUtils.isEmpty(tenantId)) {
                tenantId = getParameterValue(Constants.tenantId, "");
                if (StringUtils.isEmpty(tenantId)) {
                    throw new ClusteringFault("Azure tenantId parameter not found");
                }
            }

            if (StringUtils.isEmpty(clientId)) {
                clientId = getParameterValue(Constants.clientId, "");
                if (StringUtils.isEmpty(clientId)) {
                    throw new ClusteringFault("Azure clientId parameter not found");
                }
            }

            if (StringUtils.isEmpty(credential)) {
                credential = getParameterValue(Constants.credential, "");
                if (StringUtils.isEmpty(credential)) {
                    throw new ClusteringFault("Azure credential parameter not found");
                }
            }

            if (StringUtils.isEmpty(subscriptionId)) {
                subscriptionId = getParameterValue(Constants.subscriptionId, "");
                if (StringUtils.isEmpty(subscriptionId)) {
                    throw new ClusteringFault("Azure subscriptionId parameter not found");
                }
            }

            if (StringUtils.isEmpty(resourceGroup)) {
                resourceGroup = getParameterValue(Constants.resourceGroup, "");
                if (StringUtils.isEmpty(resourceGroup)) {
                    throw new ClusteringFault("Azure resourceGroup parameter not found");
                }
            }

            if (StringUtils.isEmpty(networkSecurityGroup)) {
                networkSecurityGroup = getParameterValue(Constants.NSG, "");
                if (StringUtils.isEmpty(networkSecurityGroup)) {
                    throw new ClusteringFault("Azure NSG parameter not found");
                }
            }

            Authentication auth = new Authentication();
            AuthenticationResult authToken = auth.getAuthToken(AUTHORIZATION_ENDPOINT, ARM_ENDPOINT, username, credential, tenantId, clientId, validationAuthority);


            log.info(String.format("Azure clustering configuration: [autherization-endpont] %s [arm-endpont] %s [tenant-id] %s [client-id] %s",
                    AUTHORIZATION_ENDPOINT, ARM_ENDPOINT, tenantId, clientId));


            List IPAdresses = new ArrayList(findVMIPaddresses(authToken, ARM_ENDPOINT, subscriptionId, resourceGroup, networkSecurityGroup));
            for (int i = 0; i < IPAdresses.size(); i++) {
                tcpIpConfig.addMember(IPAdresses.get(i).toString());
                log.info(String.format("Member added to cluster configuration: [VM-ip] %s", IPAdresses.get(i).toString()));
            }
            log.info("Azure membership scheme initialized successfully");

        } catch (Exception ex) {
            log.error(ex);
            throw new ClusteringFault("Azure membership initialization failed", ex);

        }

    }

    protected List<String> findVMIPaddresses(AuthenticationResult result, String ARM_ENDPOINT, String subscriptionID, String resourceGroup, String networkSecurityGroup) throws AzureMembershipSchemeException {
        List IPAddresses = new ArrayList();
        //list NICs grouped in the specified network security group
        String url = String.format("%ssubscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/networkSecurityGroups/%s?api-version=2016-03-30", ARM_ENDPOINT, subscriptionID, resourceGroup, networkSecurityGroup);
        InputStream instream;
        instream = getAPIresponse(url, result);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            NetworkSecurityGroup nsg = objectMapper.readValue(instream, NetworkSecurityGroup.class);
            List ninames = nsg.getProperties().getNetworkInterfaceNames();

            for (int i = 0; i < ninames.size(); i++) {

                url = String.format("%ssubscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/networkInterfaces/%s?api-version=2016-03-30", ARM_ENDPOINT, subscriptionID, resourceGroup, ninames.get(i));
                instream = getAPIresponse(url, result);
                NetworkInterface ni = objectMapper.readValue(instream, NetworkInterface.class);
                IPAddresses.add(ni.getProperties().getIPAddress());
            }

        } catch (IOException ex) {
            throw new AzureMembershipSchemeException("Could not find VM IP addresses", ex);
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

    protected String getParameterValue(String parameterName) throws ClusteringFault {
        return getParameterValue(parameterName, null);
    }

    protected String getParameterValue(String parameterName, String defaultValue) throws ClusteringFault {
        Parameter AzureServicesParam = getParameter(parameterName);
        if (AzureServicesParam == null) {
            if (defaultValue == null) {
                throw new ClusteringFault(parameterName + " parameter not found");
            } else {
                return defaultValue;
            }
        }
        return AzureServicesParam.getValue().toString();
    }

    private class AzureMembershipSchemeListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // Send all cluster messages
            carbonCluster.memberAdded(member);
            log.info(String.format("Member joined [%s] :%s", member.getUuid(), member.getSocketAddress().toString()));
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
            log.info(String.format("Member left [%s] :%s", member.getUuid(), member.getSocketAddress().toString()));
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Member attribute changed [ %s ] %s", memberAttributeEvent.getKey(), memberAttributeEvent.getValue()));
            }
        }
    }
}
