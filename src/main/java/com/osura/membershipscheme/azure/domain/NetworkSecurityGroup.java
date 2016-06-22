/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.osura.membershipscheme.azure.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Osura
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkSecurityGroup {

    private NetworkSecurityGroupProperties properties;
    private String name;

    public NetworkSecurityGroupProperties getProperties() {
        return properties;
    }

    public void setProperties(NetworkSecurityGroupProperties properties) {
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
