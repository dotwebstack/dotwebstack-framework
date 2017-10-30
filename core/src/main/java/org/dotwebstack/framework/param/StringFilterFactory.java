package org.dotwebstack.framework.param;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.param.template.TemplateProcessor;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class StringFilterFactory {

  @NonNull
  private final TemplateProcessor templateProcessor;

  StringFilter newStringFilter(IRI identifier, String name) {
    return new StringFilter(identifier, name, templateProcessor);
  }

}
