package org.dotwebstack.framework.frontend.soap.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.dotwebstack.framework.frontend.soap.wsdlreader.Constants;
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
  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestHandler.class);
  private static final String REGEX_TO_EXTRACT_SOAP_MESSAGE = "(<s.+?(?=(-{2})))";
  private static final String SOAP_ACTION = "SOAPAction";
  private static final String CONTENT_ID = "Content-ID: <http://tempuri.org/0>";
  private static final String CONTENT_TRANSFER_ENCODING_8BIT = "Content-Transfer-Encoding: 8bit";
  private static final String CONTENT_TYPE =
      "Content-Type: application/xop+xml;charset=utf-8;type=\"text/xml\"";
  private final Port wsdlPort;
  private final Definition wsdlDefinition;
  private final Map<String, SoapAction> soapActions;
  private boolean isMtom;

  public SoapRequestHandler(@NonNull Definition wsdlDefinition, @NonNull Port wsdlPort,
      @NonNull Map<String, SoapAction> soapActions, boolean isMtom) {
    this.wsdlPort = wsdlPort;
    this.wsdlDefinition = wsdlDefinition;
    this.soapActions = soapActions;
    this.isMtom = isMtom;
  }

  @Override
  public String apply(ContainerRequestContext data) {
    final String soapActionName = data.getHeaderString(SOAP_ACTION);
    String message = null;
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

    String response = message == null ? Constants.ERROR_RESPONSE : message;
    LOG.debug("Replying with the following SOAP Response: \n\n{}\n\n", response);
    return response;
  }

  private String buildSoapResponse(String message, final BindingOperation wsdlBindingOperation,
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
      if (isMtom) {
        message = addHeadersMtom(message);
      }
    } catch (Exception e) {
      LOG.warn("Unable to build SOAP message: {}", e.getMessage());
    }
    return message;
  }

  private String addHeadersMtom(final String message) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append('\n');
    stringBuilder.append("--");
    stringBuilder.append(Constants.DEFAULT_UUID);
    stringBuilder.append('\n');
    stringBuilder.append(CONTENT_ID);
    stringBuilder.append('\n');
    stringBuilder.append(CONTENT_TRANSFER_ENCODING_8BIT);
    stringBuilder.append('\n');
    stringBuilder.append(CONTENT_TYPE);
    stringBuilder.append('\n');
    stringBuilder.append('\n');

    stringBuilder.append(message);

    stringBuilder.append('\n');
    stringBuilder.append("--");
    stringBuilder.append(Constants.DEFAULT_UUID);
    stringBuilder.append("--");

    return stringBuilder.toString();
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

  private Document retrieveInputMessage(InputStream inputStream) {
    try {
      String message = getStringFromInputStream(inputStream);

      LOG.debug("Recieved the following SOAP request:\n\n {} \n \n: ", message);
      if (isMtom) {
        message = getSoapPart(message);
        LOG.debug("Extracted the following SOAP part from MTOM request:\n\n {} \n \n: ", message);
      }

      LOG.debug("Recieved the following SOAP request:\n\n {} \n \n: ", message);
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      return documentBuilder.parse(new ByteArrayInputStream(message.getBytes()));
    } catch (Exception e) {
      LOG.error("Exception during parsing of the request: {}", e.getMessage());
    }
    return null;
  }

  private String getStringFromInputStream(InputStream is) {
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

  private String getSoapPart(final String message) {
    Pattern pattern = Pattern.compile(REGEX_TO_EXTRACT_SOAP_MESSAGE);
    Matcher matcher = pattern.matcher(message);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return message;
  }
}
