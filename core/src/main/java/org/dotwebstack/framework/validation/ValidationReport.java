package org.dotwebstack.framework.validation;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class ValidationReport {

  private static final String DESCRIPTION_RESULT_MESSAGE =
      "http://www.w3.org/ns/shacl#resultMessage";

  private static final String DESCRIPTION_FOCUS_NODE = "http://www.w3.org/ns/shacl#focusNode";

  private static final String DESCRIPTION_CONFORMS = "http://www.w3.org/ns/shacl#conforms";

  private static final String DESCRIPTION_RESULT = "http://www.w3.org/ns/shacl#result";

  @java.lang.SuppressWarnings("squid:S1075")
  private static final String DESCRIPTION_RESULT_PATH = "http://www.w3.org/ns/shacl#resultPath";

  private final Model reportModel;

  private final Map<String, Violation> errors;

  public ValidationReport(@NonNull Model reportModel) {
    this.reportModel = reportModel;
    this.errors = new HashMap<>();
    parseValidationResult();
  }

  private void parseValidationResult() {
    final NodeIterator isValidProperty =
        reportModel.listObjectsOfProperty(new PropertyImpl(DESCRIPTION_CONFORMS));
    final boolean isValid = isValidProperty.next().asLiteral().getBoolean();
    if (!isValid) {
      for (Object subject : reportModel.listObjectsOfProperty(
          new PropertyImpl(DESCRIPTION_RESULT)).toSet()) {
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
        new PropertyImpl(DESCRIPTION_RESULT_PATH)).next().toString();
    final String resultMessage = model.listObjectsOfProperty(subject,
        new PropertyImpl(DESCRIPTION_RESULT_MESSAGE)).next().toString();
    final String focusNode = model.listObjectsOfProperty(subject,
        new PropertyImpl(DESCRIPTION_FOCUS_NODE)).next().toString();

    return new Violation(focusNode, resultMessage, resultPath);
  }

  public String printReport() {
    StringBuilder report = new StringBuilder();
    report.append("\n-------------------------\n");
    report.append("--- Validation report ---\n");
    report.append("-------------------------\n");
    report.append("*** Found [" + errors.size() + "] errors\n");
    for (Violation violation : errors.values()) {
      report.append(violation.getReport() + "\n");
    }
    report.append("--- ---");
    return report.toString();
  }
}
