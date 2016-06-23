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
package org.wso2.carbon.clustering.azure.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * Azure NetworkSecurityGroup NetworkInterface NetworkInterfaceProperties
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkInterfaceProperties {

    private List ipConfigurations;

    public List getIpConfigurations() {
        return ipConfigurations;
    }

    public void setIpConfigurations(List ipConfigurations) {
        this.ipConfigurations = ipConfigurations;
    }

    public String getIPAddress() {
        StringTokenizer st = new StringTokenizer(getIpConfigurations().get(0).toString(), ",");
        String ip = null;
        for (int i = 0; i < 5; i++) {
            ip = st.nextToken();
        }
        ip = ip.substring(18);

        return ip;
    }
}
