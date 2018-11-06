package org.dotwebstack.framework.frontend.soap.handlers;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.container.ContainerRequestContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SoapRequestHandlerTest { 
	
	@Mock
	ContainerRequestContext context;
	
	//service mocken?
	
	@Test
	public void testApply(){
		String soapActionName = "SOAP_ACTION";
		
		assertEquals((context.getHeaderString("SOAP_ACTION")),soapActionName);
	}

}
