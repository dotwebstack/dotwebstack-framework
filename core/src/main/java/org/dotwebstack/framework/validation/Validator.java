package org.dotwebstack.framework.validation;

public interface Validator<R, M, I> {

  void validate(I data, R shapes) throws ShaclValidationException;

  void validate(I data, R shapes, R prefixes) throws ShaclValidationException;

  void getValidationReport(M reportModel) throws ShaclValidationException;

}
