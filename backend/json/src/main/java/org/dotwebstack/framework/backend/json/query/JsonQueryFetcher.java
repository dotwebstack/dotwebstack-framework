package org.dotwebstack.framework.backend.json.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import lombok.NonNull;
import net.minidev.json.JSONArray;
import org.dotwebstack.framework.backend.json.directives.JsonDirectives;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JsonQueryFetcher implements DataFetcher<Object> {

  private ObjectMapper jsonMapper = new ObjectMapper();

  private final JsonDataService jsonDataService;

  public JsonQueryFetcher(@NonNull JsonDataService jsonDataService) {
    this.jsonDataService = jsonDataService;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    GraphQLDirective jsonDirective = environment.getFieldDefinition()
        .getDirective(JsonDirectives.JSON_NAME);

    JsonNode jsonNode = getJsonDocumentByFile(jsonDirective);

    GraphQLArgument jsonPathTemplate = jsonDirective.getArgument(JsonDirectives.ARGS_PATH);
    String jsonPath = createJsonPathWithArguments(jsonPathTemplate.getValue().toString(), environment.getArguments());

    JSONArray jsonPathResult = JsonPath.read(jsonNode.toString(), jsonPath);

    if (jsonPathResult.isEmpty()) {
      return null;
    }

    if (GraphQLTypeUtil.isList(outputType)) {
      return jsonPathResult.stream()
          .map(subject -> new JsonSolution(jsonMapper.valueToTree(subject)))
          .collect(Collectors.toList());
    }

    return new JsonSolution(jsonMapper.valueToTree(jsonPathResult.get(0)));
  }

  private JsonNode getJsonDocumentByFile(GraphQLDirective jsonDirective) {
    GraphQLArgument fileName = jsonDirective.getArgument(JsonDirectives.ARGS_FILE);

    return jsonDataService.getJsonSourceData(fileName.getValue().toString());
  }

  private String createJsonPathWithArguments(String jsonPath, Map<String, Object> arguments) {
    String result = jsonPath;

    for(Map.Entry<String, Object> entry : arguments.entrySet()) {
      result = jsonPath.replace("?", String.format("?(@.%s == %s)", entry.getKey(), entry.getValue()));
    }

    return result;
  }

}
