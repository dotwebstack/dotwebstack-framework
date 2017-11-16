package org.dotwebstack.framework.validation;

public interface Validator<R, M, I> {

  void validate(I data, R shapes) throws ShaclValidationException;

  void validate(I data, R shapes, R prefixes) throws ShaclValidationException;

  void reportValidationResult(M reportModel) throws ShaclValidationException;

}
