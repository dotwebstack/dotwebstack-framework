package org.dotwebstack.framework.frontend.soap.action;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProduct;

public class SoapAction {

  private InformationProduct informationProduct;

  private List<SoapParameter> soapParameters;

  public SoapAction(@NonNull InformationProduct informationProduct) {
    this.informationProduct = informationProduct;
    soapParameters = new ArrayList<SoapParameter>();
  }

  public void addParameter(String xpath, String parameterName) {
    soapParameters.add(new SoapParameter(xpath, parameterName));
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }
}
