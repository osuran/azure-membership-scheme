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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * Azure NetworkSecurityGroup NetworkSecurityGroupProperties
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkSecurityGroupProperties {

    private List networkInterfaces = new ArrayList();

    public List getNetworkInterfaces() {
        return networkInterfaces;
    }

    public void setNetworkInterfaces(List networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

    public List getNetworkInterfaceNames() {
        StringTokenizer[] st = new StringTokenizer[networkInterfaces.size()];
        String[] NICname = new String[networkInterfaces.size()];
        List names = new ArrayList();
        for (int i = 0; i < networkInterfaces.size(); i++) {
            st[i] = new StringTokenizer(networkInterfaces.get(i).toString(), "/");
            while (st[i].hasMoreTokens()) {
                NICname[i] = st[i].nextToken();
            }
            NICname[i] = NICname[i].substring(0, NICname[i].length() - 1);
            names.add(NICname[i]);
        }

        return names;
    }
}
