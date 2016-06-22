/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.osura.membershipscheme.azure.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 *
 * @author Osura
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkInterface {

    private NetworkInterfaceProperties properties;


    public NetworkInterfaceProperties getProperties() {
        return properties;
    }

    public void setProperties(NetworkInterfaceProperties properties) {
        this.properties = properties;
    }
}
