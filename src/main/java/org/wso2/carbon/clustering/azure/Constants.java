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

/**
 *
 * Constants for Azure membership scheme
 */
public class Constants {

    public final static String AUTHORIZATION_ENDPOINT = "https://login.microsoftonline.com/"; 
    public final static String ARM_ENDPOINT = "https://management.azure.com/"; 
    public final static String REST_API_AVAILABLE_NICs = "%ssubscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/networkSecurityGroups"
            + "/%s?api-version=2016-03-30";
    public final static String REST_API_NIC_INFO = "%ssubscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/networkInterfaces"
            + "/%s?api-version=2016-03-30";
    public final static String azure_username = "azure_username";
    public final static String azure_credential = "credential";
    public final static String azure_tenantId = "tenantId";
    public final static String azure_clientId = "clientId";
    public final static String azure_subscriptionId = "subscriptionId";
    public final static String azure_resourceGroup = "resourceGroup";
    public final static String azure_networkSecurityGroup = "NSG";
    public final static String validationAuthorityValue = "validationAuthority"; 
}
