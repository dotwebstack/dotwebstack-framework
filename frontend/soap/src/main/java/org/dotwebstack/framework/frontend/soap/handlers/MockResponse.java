package org.dotwebstack.framework.frontend.soap.handlers;

public class MockResponse {
  
  public static final String GET_VEHICLE_NAMES = "<?xml version=\"1.0\"?>"
      + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\">"
      + "  <soap:Body xmlns:m=\"http://www.example.org/soaptest\">"
      + "    <m:GetTestResult>"
      + "      <m:Value>TEST!</m:Value>"
      + "    </m:GetTestResult>"
      + "  </soap:Body>"
      + "</soap:Envelope>";

}