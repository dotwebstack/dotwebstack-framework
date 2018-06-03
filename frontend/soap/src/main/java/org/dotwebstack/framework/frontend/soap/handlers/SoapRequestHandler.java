package org.dotwebstack.framework.frontend.soap.handlers;

import javax.ws.rs.container.ContainerRequestContext;
import javax.wsdl.Port;
import lombok.NonNull;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapRequestHandler implements Inflector<ContainerRequestContext, String> {

  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestHandler.class);

  private final Port wsdlPort;
  
  private static final String ERROR_RESPONSE = "<?xml version=\"1.0\"?>"
      + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
      + "  <s:Body>"
      + "    <s:Fault>"
      + "      <faultcode xmlns:a=\"http://schemas.microsoft.com/ws/2005/05/addressing/none\">"
          + "a:ActionNotSupported</faultcode>"
      + "      <faultstring xml:lang=\"en-US\">The message with Action '%s' cannot be processed "
          + "at the receiver, due to a ContractFilter mismatch at the EndpointDispatcher. This may "
          + "be because of either a contract mismatch (mismatched Actions between sender and "
          + "receiver) or a binding/security mismatch between the sender and the receiver.  Check "
          + "that sender and receiver have the same contract and the same binding (including "
          + "security requirements, e.g. Message, Transport, None).</faultstring>"
      + "    </s:Fault>"
      + "  </s:Body>"
      + "</s:Envelope>";
      
  public SoapRequestHandler(@NonNull Port wsdlPort) {
    this.wsdlPort = wsdlPort;
  }

  @Override
  public String apply(ContainerRequestContext data) {
    final String soapAction = data.getHeaderString("SOAPAction");
    LOG.debug("Handling SOAP request, SOAPAction: {}",soapAction);
    if (soapAction.equals(String.format("\"%s\"",SoapAction.GET_VEHICLE_NAMES))) {
      return MockResponse.GET_VEHICLE_NAMES;
    } else {
      return ERROR_RESPONSE;
    }
  }

}
