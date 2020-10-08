package org.dotwebstack.framework.backend.json.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.dotwebstack.framework.backend.json.scalars.JsonScalars;
import org.dotwebstack.framework.core.converters.CoreConverterRouter;
import org.dotwebstack.framework.core.datafetchers.SourceDataFetcher;
import org.dotwebstack.framework.core.helpers.TypeHelper;

public class JsonValueFetcher extends SourceDataFetcher {

  private final ObjectMapper mapper = new ObjectMapper();

  public JsonValueFetcher(CoreConverterRouter converterRouter) {
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

    if (JsonScalars.OBJECT.getName()
        .equals(TypeHelper.getTypeName(dataFetchingEnvironment.getFieldDefinition()
            .getType()))) {
      return mapper.<Map<String, Object>>convertValue(fieldNode, new TypeReference<>() {});
    }

    if (fieldNode.isArray() && fieldNode.size() > 0) {
      return StreamSupport.stream(fieldNode.spliterator(), false)
          .map(subject -> subject.isObject() ? subject : subject.asText())
          .collect(Collectors.toList());
    }

    return fieldNode.isValueNode() ? fieldNode.asText() : new JsonSolution(fieldNode);
  }
}
