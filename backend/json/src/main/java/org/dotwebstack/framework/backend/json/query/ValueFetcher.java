package org.dotwebstack.framework.backend.json.query;

import com.fasterxml.jackson.databind.JsonNode;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import org.dotwebstack.framework.core.converters.CoreConverterRouter;
import org.dotwebstack.framework.core.datafetchers.SourceDataFetcher;

import java.util.ArrayList;
import java.util.List;

public class ValueFetcher extends SourceDataFetcher {

  public ValueFetcher(CoreConverterRouter converterRouter) {
    super(converterRouter);
  }

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return (environment.getSource() instanceof JsonSolution);
  }

  @Override
  public Object get(DataFetchingEnvironment dataFetchingEnvironment) throws Exception {
    JsonSolution jsonSolution = dataFetchingEnvironment.getSource();
    JsonNode result = jsonSolution.getJsonNode();

    Field field = dataFetchingEnvironment.getField();
    String name = field.getName();
    JsonNode fieldNode = result.get(name);
    if (fieldNode == null || fieldNode.isNull()) {
      return null;
    }

    if (fieldNode.isArray() && fieldNode.size() > 0 && fieldNode.get(0)
        .isValueNode()) {
      List<Object> items = new ArrayList<>();
      fieldNode.forEach(item -> items.add(item.asText()));
      return items;
    }

    return fieldNode.isValueNode() ? fieldNode.asText() : new JsonSolution(fieldNode);
  }
}
