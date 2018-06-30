package org.dotwebstack.framework.frontend.soap.action;

public class SoapParameter {

  private String xpath;

  private String parameterName;

  public SoapParameter(String xpath, String parameterName) {
    this.xpath = xpath;
    this.parameterName = parameterName;
  }

  public String getParameterName() {
    return parameterName;
  }

  public String getXpath() {
    return xpath;
  }
}
