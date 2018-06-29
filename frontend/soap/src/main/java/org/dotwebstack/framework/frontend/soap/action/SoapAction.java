package org.dotwebstack.framework.frontend.soap.action;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.soap.wsdlreader.WsdlNamespaceContext;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SoapAction {

  private static final Logger LOG = LoggerFactory.getLogger(SoapAction.class);

  private static final String XPATH_TEMPLATE = "soapenv:Envelope/soapenv:Body"
      + "/*[local-name()='%s']/*[local-name()='%s']/*[local-name()='%s']";

  private String soapAction;

  private InformationProduct informationProduct;

  private List<SoapParameter> soapParameters;

  public SoapAction(String soapAction, @NonNull InformationProduct informationProduct) {
    this.soapAction = soapAction;
    this.informationProduct = informationProduct;
    soapParameters = new ArrayList<SoapParameter>();
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

  private void addParameter(String xpath, String parameterName) {
    soapParameters.add(new SoapParameter(xpath, parameterName));
  }

  public void retrieveParameters(Element typesElement) {
    try {
      //Factory and Xpath may be part of construction
      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();
      xpath.setNamespaceContext(new WsdlNamespaceContext());
      LOG.debug("| Root element: " + soapAction);
      XPathExpression expr = xpath.compile("xs:schema/xs:element[@name='"
          + soapAction + "']/xs:complexType/xs:sequence/xs:element");
      Element mainElement = (Element) expr.evaluate(typesElement, XPathConstants.NODE);
      if (mainElement == null) {
        LOG.warn("| Root element not found: Action not defined in types");
        return;
      }
      String firstElement = mainElement.getAttribute("name");
      LOG.debug("| First element: " + firstElement);
      String paramName = mainElement.getAttribute("type");
      String[] splitted = paramName.split(":");
      if (splitted.length == 2) {
        paramName = splitted[1];
      }
      XPathExpression exprt = xpath.compile("xs:schema/xs:element[@name='"
          + soapAction + "']/@name");
      XPathExpression expr2 = xpath.compile("xs:schema/xs:complexType[@name='"
          + paramName + "']/xs:complexContent/xs:extension/xs:sequence/xs:element");
      NodeList nodes = (NodeList) expr2.evaluate(typesElement, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); i++) {
        XPathExpression expr3 = xpath.compile("xs:annotation/@dws:parameter");
        String param = (String) expr3.evaluate(nodes.item(i), XPathConstants.STRING);
        if (!param.isEmpty()) {
          LOG.debug("| Input element: " + ((Element) nodes.item(i)).getAttribute("name"));
          LOG.debug("| - DWS-parameter: " + param);
          String xpathString = String.format(SoapAction.XPATH_TEMPLATE, soapAction, firstElement,
              ((Element) nodes.item(i)).getAttribute("name"));
          LOG.debug("| - xpathString: " + xpathString);
          addParameter(xpathString, param);
        }
      }
    } catch (XPathExpressionException e) {
      LOG.error(e.getMessage());
    }
  }

}
