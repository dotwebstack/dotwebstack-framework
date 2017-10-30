package org.dotwebstack.framework.param;

import java.util.Collections;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

class StringFilter extends AbstractParameter {

  private final TemplateProcessor templateProcessor;

  StringFilter(IRI identifier, String name, @NonNull TemplateProcessor templateProcessor) {
    super(identifier, name);

    this.templateProcessor = templateProcessor;
  }

  @Override
  public String handle(String value, @NonNull String query) {
    return templateProcessor.processString(query, Collections.singletonMap(getName(), value));
  }

}
