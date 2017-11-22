package org.dotwebstack.framework.validation;

import lombok.NonNull;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class ValidationReport {

  private final Model reportModel;

  private boolean isValid;

  private String focusNode;

  private String resultMessage;

  private String resultPath;

  public ValidationReport(@NonNull Model reportModel) {
    this.reportModel = reportModel;
    this.isValid = false;
    this.focusNode = "";
    this.resultMessage = "";
    this.resultPath = "";
    parseValidationResult();
  }

  public boolean isValid() {
    return isValid;
  }

  public String getValidationReport() {
    if (!isValid) {
      return String.format("Invalid configuration at path [%s] on node [%s] with error "
          + "message [%s]", resultPath, focusNode, resultMessage);
    } else {
      return "Everything is okay.";
    }
  }

  private void parseValidationResult() {
    final NodeIterator isValidProperty = reportModel.listObjectsOfProperty(new PropertyImpl("http://www.w3.org/ns/shacl#conforms"));
    isValid = isValidProperty.next().asLiteral().getBoolean();
    if (!isValid) {
      final NodeIterator resultPathProperty = reportModel.listObjectsOfProperty(new PropertyImpl("http://www.w3.org/ns/shacl#resultPath"));
      resultPath = resultPathProperty.next().toString();
      final NodeIterator resultMessageProperty = reportModel.listObjectsOfProperty(new PropertyImpl("http://www.w3.org/ns/shacl#resultMessage"));
      resultMessage = resultMessageProperty.next().asLiteral().getString();
      final NodeIterator focusNodeProperty = reportModel.listObjectsOfProperty(new PropertyImpl("http://www.w3.org/ns/shacl#focusNode"));
      focusNode = focusNodeProperty.next().toString();
    }
  }
}
