/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.osura.membershipscheme.azure.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Osura
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
