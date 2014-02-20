package com.hiperf.gwtjpa.test.client;

import org.agoncal.application.petstore.client.PetStoreTestIntegration;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.gwt.junit.tools.GWTTestSuite;
import com.hiperf.gwtjpa.test.table.basic.client.BasicTestIntegration;
import com.hiperf.gwtjpa.test.table.msgprovider.client.MsgProviderTestIntegration;

public class ClientTestSuite extends GWTTestSuite {

	public static Test suite() {
        TestSuite suite = new TestSuite("Test for a Maps Application");
        suite.addTestSuite(BasicTestIntegration.class);
        suite.addTestSuite(PetStoreTestIntegration.class);
        suite.addTestSuite(MsgProviderTestIntegration.class);
        return suite;
    }
	
	
}
