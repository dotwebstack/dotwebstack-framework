package org.dotwebstack.framework.frontend.soap.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.soap.action.SoapAction;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SchemaDefinitionWrapper;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SoapContext;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SoapUtils;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


public class SoapRequestHandler implements Inflector<ContainerRequestContext, String> {

  static final String ERROR_RESPONSE = "<?xml version=\"1.0\"?>"
      + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "  <s:Body>"
      + "    <s:Fault>"
      + "      <faultcode xmlns:a=\"http://schemas.microsoft.com/ws/2005/05/addressing/none\">"
      + "a:ActionNotSupported</faultcode>"
      + "      <faultstring xml:lang=\"en-US\">The message with Action '%s' cannot be processed "
      + "at the receiver, due to a ContractFilter mismatch at the EndpointDispatcher. This may "
      + "be because of either a contract mismatch (mismatched Actions between sender and "
      + "receiver) or a binding/security mismatch between the sender and the receiver.  Check "
      + "that sender and receiver have the same contract and the same binding (including "
      + "security requirements, e.g. Message, Transport, None).</faultstring>" + "    </s:Fault>"
      + "  </s:Body>" + "</s:Envelope>";
  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestHandler.class);
  private static final String REGEX_TO_REMOVE_MULTIPART = "^C.+?(?=(<soapenv))";
  private final Definition wsdlDefinition;
  final Port wsdlPort;
  private final Map<String, SoapAction> soapActions;

  public SoapRequestHandler(@NonNull Definition wsdlDefinition, @NonNull Port wsdlPort,
      @NonNull Map<String, SoapAction> soapActions) {
    this.wsdlPort = wsdlPort;
    this.wsdlDefinition = wsdlDefinition;
    this.soapActions = soapActions;
  }

  @Override
  public String apply(ContainerRequestContext data) {
    final String soapActionName = data.getHeaderString("SOAPAction");
    String message = ERROR_RESPONSE;
    LOG.debug("Handling SOAP XML request, SOAPAction: {}", soapActionName);

    BindingOperation wsdlBindingOperation =
        SoapUtils.findWsdlBindingOperation(wsdlPort, soapActionName);
    if (wsdlBindingOperation == null) {
      // No operation found. Return the error message.
      LOG.warn("Not found BindingOperation: {}", soapActionName);
    } else {
      // Retrieve the input message for parameters
      Document inputDoc = null;
      if (data.hasEntity()) {
        inputDoc = retrieveInputMessage(data.getEntityStream());
      }
      message = buildSoapResponse(message, wsdlBindingOperation, inputDoc);
    }

    return message == null ? ERROR_RESPONSE : message;
  }

  String buildSoapResponse(String message, final BindingOperation wsdlBindingOperation,
      final Document inputDoc) {
    SoapAction soapAction = soapActions.get(wsdlBindingOperation.getName());
    TupleQueryResult queryResult = null;
    if (soapAction == null) {
      LOG.warn("No information product found - revert to mocking.");
    } else {
      queryResult = getResult(soapAction, inputDoc);
    }
    try {
      message = SoapUtils.buildSoapMessageFromOutput(new SchemaDefinitionWrapper(wsdlDefinition),
          wsdlPort.getBinding(), wsdlBindingOperation, SoapContext.DEFAULT, queryResult);
    } catch (Exception e) {
      LOG.warn("Unable to build SOAP message: {}", e.getMessage());
    }
    return message;
  }

  private TupleQueryResult getResult(SoapAction soapAction, Document inputDoc) {
    InformationProduct informationProduct = soapAction.getInformationProduct();
    Map<String, String> parameterValues = soapAction.getParameterValues(inputDoc);
    Object result = informationProduct.getResult(parameterValues);

    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      return (TupleQueryResult) result;
    }

    throw new ConfigurationException(String
        .format("Result type %s not supported for information product %s",
            informationProduct.getResultType(), informationProduct.getIdentifier()));
  }

  Document retrieveInputMessage(InputStream inputStream) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      String stringFromInputStream = getStringFromInputStream(inputStream);
      String stringWithoutHeader = removeXmlHeader(stringFromInputStream);
      byte[] message = stringWithoutHeader.getBytes();

      LOG.debug("Recieved the following request:\n\n {} \n \n: ", stringWithoutHeader);
      return builder.parse(new ByteArrayInputStream(message));
    } catch (Exception e) {
      LOG.error("Exception during parsing of the request: {}", e.getMessage());
    }
    return null;
  }

  String getStringFromInputStream(InputStream is) {
    StringBuilder sb = new StringBuilder();

    String line;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }

    } catch (IOException e) {
      LOG.error("Error converting message to string {}", e.getMessage());
    }
    return sb.toString();
  }

  private String removeXmlHeader(String xml) {
    if (xml != null) {
      xml = xml.replaceAll(REGEX_TO_REMOVE_MULTIPART, "").trim();
    }
    return xml;
  }
}
