package org.dotwebstack.framework.templating.pebble.filter;

import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.springframework.stereotype.Component;

@Component
public class JsonLdFilter implements Filter {

  @Override
  public List<String> getArgumentNames() {
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object apply(Object model, Map<String, Object> map, PebbleTemplate pebbleTemplate,
      EvaluationContext evaluationContext, int i) {
    var stringWriter = new StringWriter();
    var rdfWriter = Rio.createWriter(RDFFormat.JSONLD, stringWriter);

    rdfWriter.getWriterConfig()
        .set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
    rdfWriter.getWriterConfig()
        .set(JSONLDSettings.OPTIMIZE, true);

    Rio.write((Iterable<Statement>) model, rdfWriter);
    return stringWriter.toString();
  }
}
