package org.dotwebstack.framework.frontend.soap.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.container.ContainerRequestContext;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.soap.action.SoapAction;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SoapUtils;
import org.glassfish.jersey.message.internal.DataSourceProvider;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


public class SoapRequestHandlerMtom extends SoapRequestHandler
    implements Inflector<ContainerRequestContext, String> {

  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestHandlerMtom.class);
  private static final String MULTIPART_RELATED = "multipart/related";
  private static final String UTF_8 = "utf-8";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String APPLICATION_XOP_XML_CHARSET_UTF_8_TYPE_TEXT_XML =
      "application/xop+xml;charset=utf-8;type=\"text/xml\"";
  private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
  private static final String EIGHT_BIT = "8bit";
  private static final String SOAP_ACTION = "SOAPAction";

  public SoapRequestHandlerMtom(@NonNull Definition wsdlDefinition, @NonNull Port wsdlPort,
      @NonNull Map<String, SoapAction> soapActions) {
    super(wsdlDefinition, wsdlPort, soapActions);
  }

  @Override
  public String apply(ContainerRequestContext data) {
    final String soapActionName = data.getHeaderString(SOAP_ACTION);
    String msg = ERROR_RESPONSE;
    LOG.debug("Handling SOAP Multipart XOP request, SOAPAction: {}", soapActionName);

    BindingOperation wsdlBindingOperation =
        SoapUtils.findWsdlBindingOperation(wsdlPort, soapActionName);
    if (wsdlBindingOperation == null) {
      // No operation found. Return the error message.
      LOG.warn("Not found BindingOperation: {}", soapActionName);
    } else {
      // Retrieve the input message for parameters
      Document inputDocument = getInputDocument(data);
      msg = buildSoapResponse(msg, wsdlBindingOperation, inputDocument);
    }

    return createResponse(msg);
  }

  private String createResponse(String msg) {
    MimeMultipart mimeMultipart = new MimeMultipart();
    MimeBodyPart textPart = new MimeBodyPart();
    try {
      textPart.setText(msg, UTF_8);
      textPart.addHeader(CONTENT_TYPE, APPLICATION_XOP_XML_CHARSET_UTF_8_TYPE_TEXT_XML);
      textPart.addHeader(CONTENT_TRANSFER_ENCODING, EIGHT_BIT);
      mimeMultipart.addBodyPart(textPart);
    } catch (MessagingException e) {
      LOG.error("Error creating Mime Response: {}", e.getMessage());
    }
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      mimeMultipart.writeTo(baos);
      return getStringFromInputStream(new ByteArrayInputStream(baos.toByteArray()));
    } catch (Exception ex) {
      LOG.warn("Error convertin MimeMultipart to String: {}", ex.getMessage());
    }
    return ERROR_RESPONSE;

  }

  private ByteArrayInputStream mimeParser(InputStream isMtm) {

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      MimeMultipart mp =
          new MimeMultipart(new DataSourceProvider.ByteArrayDataSource(isMtm, MULTIPART_RELATED));
      BodyPart bodyPart = mp.getBodyPart(0);

      bodyPart.writeTo(baos);

      return new ByteArrayInputStream(baos.toByteArray());
    } catch (Exception ex) {
      LOG.warn("Error extracting soapbody: {}", ex.getMessage());
    }
    return null;
  }

  private Document getInputDocument(final ContainerRequestContext data) {
    if (data.hasEntity()) {
      return retrieveInputMessage(mimeParser(data.getEntityStream()));
    }
    return null;
  }
}
