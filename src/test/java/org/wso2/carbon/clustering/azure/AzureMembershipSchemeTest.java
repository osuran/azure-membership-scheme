/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.clustering.azure;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.microsoft.aad.adal4j.AuthenticationResult;
import java.io.InputStream;
import java.util.List;
import junit.framework.TestCase;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;

/**
 *
 * @author Osura
 */
public class AzureMembershipSchemeTest extends TestCase {
    
    public AzureMembershipSchemeTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of setPrimaryHazelcastInstance method, of class AzureMembershipScheme.
     */
    public void testSetPrimaryHazelcastInstance() {
        System.out.println("setPrimaryHazelcastInstance");
        HazelcastInstance primaryHazelcastInstance = null;
        AzureMembershipScheme instance = null;
        instance.setPrimaryHazelcastInstance(primaryHazelcastInstance);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLocalMember method, of class AzureMembershipScheme.
     */
    public void testSetLocalMember() {
        System.out.println("setLocalMember");
        Member localMember = null;
        AzureMembershipScheme instance = null;
        instance.setLocalMember(localMember);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCarbonCluster method, of class AzureMembershipScheme.
     */
    public void testSetCarbonCluster() {
        System.out.println("setCarbonCluster");
        HazelcastCarbonClusterImpl hazelcastCarbonCluster = null;
        AzureMembershipScheme instance = null;
        instance.setCarbonCluster(hazelcastCarbonCluster);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of init method, of class AzureMembershipScheme.
     */
    public void testInit() throws Exception {
        System.out.println("init");
        AzureMembershipScheme instance = null;
        instance.init();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findVMIPaddresses method, of class AzureMembershipScheme.
     */
    public void testFindVMIPaddresses() throws Exception {
        System.out.println("findVMIPaddresses");
        AuthenticationResult result_2 = null;
        String ARM_ENDPOINT = "";
        String subscriptionID = "";
        String resourceGroup = "";
        String networkSecurityGroup = "";
        AzureMembershipScheme instance = null;
        List expResult = null;
        List result = instance.findVMIPaddresses(result_2, ARM_ENDPOINT, subscriptionID, resourceGroup, networkSecurityGroup);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAPIresponse method, of class AzureMembershipScheme.
     */
    public void testGetAPIresponse() throws Exception {
        System.out.println("getAPIresponse");
        String url = "";
        AuthenticationResult result_2 = null;
        AzureMembershipScheme instance = null;
        InputStream expResult = null;
        InputStream result = instance.getAPIresponse(url, result_2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of joinGroup method, of class AzureMembershipScheme.
     */
    public void testJoinGroup() throws Exception {
        System.out.println("joinGroup");
        AzureMembershipScheme instance = null;
        instance.joinGroup();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConstant method, of class AzureMembershipScheme.
     */
    public void testGetConstant() throws Exception {
        System.out.println("getConstant");
        String constant = "";
        String defaultValue = "";
        boolean isOptional = false;
        AzureMembershipScheme instance = null;
        String expResult = "";
        String result = instance.getConstant(constant, defaultValue, isOptional);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
