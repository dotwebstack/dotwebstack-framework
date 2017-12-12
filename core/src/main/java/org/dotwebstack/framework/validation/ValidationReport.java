package org.dotwebstack.framework.validation;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class ValidationReport {

  private final Model reportModel;

  private final Map<String, Violation> errors;

  private final String descriptionResultPath = "http://www.w3.org/ns/shacl#resultPath";

  private final String descriptionResultMessage = "http://www.w3.org/ns/shacl#resultMessage";

  private final String descriptionFocusNode = "http://www.w3.org/ns/shacl#focusNode";

  private final String descriptionConforms = "http://www.w3.org/ns/shacl#conforms";

  private final String descriptionResult = "http://www.w3.org/ns/shacl#result";

  public ValidationReport(@NonNull Model reportModel) {
    this.reportModel = reportModel;
    this.errors = new HashMap<>();
    parseValidationResult();
  }

  private void parseValidationResult() {
    final NodeIterator isValidProperty =
        reportModel.listObjectsOfProperty(new PropertyImpl(descriptionConforms));
    final boolean isValid = isValidProperty.next().asLiteral().getBoolean();
    if (!isValid) {
      for (Object subject : reportModel.listObjectsOfProperty(
          new PropertyImpl(descriptionResult)).toSet()) {
        errors.put(subject.toString(), createErrorObject(reportModel, (Resource) subject));
      }
    }
  }

  public Map<String, Violation> getErrors() {
    return errors;
  }

  public boolean isValid() {
    return errors.size() > 0 ? false : true;
  }

  private Violation createErrorObject(Model model, Resource subject) {
    final String resultPath = model.listObjectsOfProperty(subject,
        new PropertyImpl(descriptionResultPath)).next().toString();
    final String resultMessage = model.listObjectsOfProperty(subject,
        new PropertyImpl(descriptionResultMessage)).next().toString();
    final String focusNode = model.listObjectsOfProperty(subject,
        new PropertyImpl(descriptionFocusNode)).next().toString();

    return new Violation(focusNode, resultMessage, resultPath);
  }

  public String printReport() {
    StringBuilder report = new StringBuilder();
    report.append("\n--- Validation report ---\n");
    for (Violation violation : errors.values()) {
      report.append(violation.getErrorReport() + "\n");
    }
    report.append("\n--- ---");
    return report.toString();
  }
}
