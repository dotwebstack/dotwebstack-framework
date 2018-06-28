package org.dotwebstack.framework.frontend.soap.handlers;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;

import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SchemaDefinitionWrapper;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SoapContext;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SoapUtils;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapRequestHandler implements Inflector<ContainerRequestContext, String> {

  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestHandler.class);

  private final Definition wsdlDefinition;

  private final Port wsdlPort;

  private final Map<String, InformationProduct> informationProducts;

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

  public SoapRequestHandler(@NonNull Definition wsdlDefinition, @NonNull Port wsdlPort,
      @NonNull Map<String, InformationProduct> informationProducts) {
    this.wsdlPort = wsdlPort;
    this.wsdlDefinition = wsdlDefinition;
    this.informationProducts = informationProducts;
  }

  @Override
  public String apply(ContainerRequestContext data) {
    final String soapAction = data.getHeaderString("SOAPAction");
    String msg = ERROR_RESPONSE;
    LOG.debug("Handling SOAP request, SOAPAction: {}",soapAction);

    BindingOperation wsdlBindingOperation =
        SoapUtils.findWsdlBindingOperation(wsdlDefinition, wsdlPort, soapAction);
    if (wsdlBindingOperation == null) {
      // No operation found. Return the error message.
      LOG.warn("Not found BindingOperation: {}", soapAction);
    } else {
      // Build the SOAP response for the specific message
      InformationProduct informationProduct = informationProducts.get(
          wsdlBindingOperation.getName());
      TupleQueryResult queryResult = null;
      if (informationProduct == null) {
        LOG.warn("No information product found - revert to mocking.");
      } else {
        queryResult = getResult(informationProduct);
      }
      try {
        msg = SoapUtils.buildSoapMessageFromOutput(
            new SchemaDefinitionWrapper(wsdlDefinition),
            wsdlPort.getBinding(),
            wsdlBindingOperation,
            SoapContext.DEFAULT,
            queryResult);
      } catch (Exception e) {
        LOG.warn("Unable to build SOAP message: {}", e.getMessage());
      }
    }

    return msg;
  }

  private TupleQueryResult getResult(InformationProduct informationProduct) {
    //TODO: extract parameter values from input message
    Map<String, String> parameterValues = new HashMap();
    Object result = informationProduct.getResult(parameterValues);

    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      return (TupleQueryResult) result;
    }

    throw new ConfigurationException(
        String.format("Result type %s not supported for information product %s",
            informationProduct.getResultType(), informationProduct.getIdentifier()));
  }

}
