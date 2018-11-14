package org.dotwebstack.framework.frontend.soap.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;

import org.dotwebstack.framework.frontend.soap.action.SoapAction;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

  private static final String REQUEST = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetDomainTable xmlns=\"http://rws.services.nl/DomainTableWS/2010/10\"><request xmlns:a=\"http://rws.services.nl/DomainTableWS/Contracts/2010/10\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><a:PageSize>2500</a:PageSize><a:StartPage>0</a:StartPage><a:CheckDate>2018-11-02T10:21:20.3569725+01:00</a:CheckDate><a:DomaintableName>Compartiment</a:DomaintableName></request></GetDomainTable></s:Body></s:Envelope>";
  private static final String SOAP_ACTION = "SOAPAction";

  @Mock
  private ContainerRequestContext context;

  @Mock
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
  public void before() {
    soapActions = new HashMap<>();
    soapRequestHandler = new SoapRequestHandler(wsdlDefinition, wsdlPort, soapActions, false);
  }

  @Test
  public void shouldReturnErrorMessageWhenWsdlBindingOperationIsNull() {
    when(context.getHeaderString(SOAP_ACTION)).thenReturn("SoapActionName");

    String response = soapRequestHandler.apply(context);
    assertThat(response, is(ERROR_RESPONSE));
  }

  @Test
  public void shouldReturnErrorMessageWhenDataHasNoEntity() {
    //wsdlPort vullen met String name en Binding binding
    List<BindingOperation> wsdlBindingOperations = new ArrayList<>();

    when(bindingOperation.getName()).thenReturn("GetDomainTableNames");

    wsdlBindingOperations.add(bindingOperation);

    when(binding.getBindingOperations()).thenReturn(wsdlBindingOperations);
    when(wsdlPort.getBinding()).thenReturn(binding);
    when(context.getHeaderString(SOAP_ACTION)).thenReturn("/GetDomainTableNames\"");

    String response = soapRequestHandler.apply(context);

    assertThat(response, is(ERROR_RESPONSE));
  }

  //@Test
  public void shouldReturnValidMessageWhenInputDocIsAvailable() {
    List<BindingOperation> wsdlBindingOperations = new ArrayList<>();

    when(bindingOperation.getName()).thenReturn("GetDomainTableNames");

    wsdlBindingOperations.add(bindingOperation);

    when(binding.getBindingOperations()).thenReturn(wsdlBindingOperations);
    when(wsdlPort.getBinding()).thenReturn(binding);
    when(context.getHeaderString(SOAP_ACTION)).thenReturn("/GetDomainTableNames\"");

    when(context.hasEntity()).thenReturn(true);

    InputStream inputstream = new ByteArrayInputStream(REQUEST.getBytes(StandardCharsets.UTF_8));

    when(context.getEntityStream()).thenReturn(inputstream);

    InformationProduct informationProduct = mock(InformationProduct.class);
    SoapAction soapAction = new SoapAction("GetDomainTableNames", informationProduct);
    soapActions.put("GetDomainTableNames", soapAction);
    String response = soapRequestHandler.apply(context);

    assertThat(response, is(""));
  }
}
