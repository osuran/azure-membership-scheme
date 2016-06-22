/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.osura.membershipscheme.azure.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Osura
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
