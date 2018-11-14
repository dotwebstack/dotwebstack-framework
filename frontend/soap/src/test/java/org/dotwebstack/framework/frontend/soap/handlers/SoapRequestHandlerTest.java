package org.dotwebstack.framework.frontend.soap.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.dotwebstack.framework.frontend.soap.action.SoapAction;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.xml.sax.InputSource;

@RunWith(MockitoJUnitRunner.class)
public class SoapRequestHandlerTest {

  private static final String ERROR_RESPONSE = "<?xml version=\"1.0\"?>"
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
  private static final String HTTP_NAMESPACES_SNOWBOARD_INFO_COM_ENDORSEMENT_SEARCH_SERVICE =
      "{http://namespaces.snowboard-info.com}EndorsementSearchService";
  private static final String SOAP_ACTION = "SOAPAction";
  private String request;
  private String response;
  @Mock
  private ContainerRequestContext context;

  private Definition wsdlDefinition;

  @Mock
  private Port wsdlPort;

  @Mock
  private BindingOperation bindingOperation;

  @Mock
  private Binding binding;

  private Map<String, SoapAction> soapActions;

  private SoapRequestHandler soapRequestHandler;

  @Before
  public void before() throws WSDLException, IOException, URISyntaxException {
    wsdlDefinition = createWsdlDefinition();
    Service service = (Service) new ArrayList<>(wsdlDefinition.getServices().values()).get(0);
    this.wsdlPort = service.getPort("GetEndorsingBoarderPort");
    request = readResource("request.xml");
    response = readResource("response.xml");

    soapActions = new HashMap<>();
    soapRequestHandler = new SoapRequestHandler(wsdlDefinition, this.wsdlPort, soapActions, false);
  }

  @Test
  public void shouldReturnErrorMessageWhenWsdlBindingOperationIsNull() {
    when(context.getHeaderString(SOAP_ACTION)).thenReturn("SoapActionName");

    String response = soapRequestHandler.apply(context);
    assertThat(response, is(ERROR_RESPONSE));
  }

  @Test
  public void shouldReturnErrorMessageWhenDataHasNoEntity() {

    when(context.getHeaderString(SOAP_ACTION)).thenReturn("/GetDomainTableNames\"");

    String response = soapRequestHandler.apply(context);

    assertThat(response, is(ERROR_RESPONSE));
  }

  @Ignore
  public void shouldReturnValidMessageWhenInputDocIsAvailable()
      throws WSDLException, URISyntaxException, IOException {

    when(context.getHeaderString(SOAP_ACTION)).thenReturn("/GetDomainTableNames\"");

    when(context.hasEntity()).thenReturn(true);

    InputStream inputstream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));

    when(context.getEntityStream()).thenReturn(inputstream);

    InformationProduct informationProduct = mock(InformationProduct.class);
    SoapAction soapAction = new SoapAction("GetDomainTableNames", informationProduct);
    soapActions.put("GetDomainTableNames", soapAction);
    String response = soapRequestHandler.apply(context);

    assertThat(response, is(""));
  }

  private Definition createWsdlDefinition() throws WSDLException, IOException, URISyntaxException {
    WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
    String wsdl = readResource("wsdl.xsd");

    return wsdlReader.readWSDL("http://example.org/", new InputSource(new StringReader(wsdl)));
  }

  private String readResource(String name) throws IOException, URISyntaxException {
    return new String(
        Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())));
  }

}
