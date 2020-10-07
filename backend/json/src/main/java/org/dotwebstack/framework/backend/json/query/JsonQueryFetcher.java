package org.dotwebstack.framework.backend.json.query;

import static com.jayway.jsonpath.Criteria.where;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.tuple.Pair;
import org.dotwebstack.framework.backend.json.directives.JsonDirectives;
import org.dotwebstack.framework.backend.json.directives.PredicateDirectives;
import org.springframework.stereotype.Component;

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
    List<Filter> jsonPathFilters =
        createJsonPathWithArguments(environment.getArguments(), environment.getFieldDefinition());

    JSONArray jsonPathResult = JsonPath.parse(jsonNode.toString())
        .read(jsonPathTemplate.getValue()
            .toString(), jsonPathFilters.toArray(new Filter[jsonPathFilters.size()]));

    if (jsonPathResult.isEmpty()) {
      return null;
    }

    if (GraphQLTypeUtil.isList(outputType)) {
      return jsonPathResult.stream()
          .flatMap(subject -> {
            if (subject instanceof JSONArray) {
              return ((JSONArray) subject).stream();
            }
            return Stream.of(subject);
          })
          .map(subject -> new JsonSolution(jsonMapper.valueToTree(subject)))
          .collect(Collectors.toList());
    }

    return new JsonSolution(jsonMapper.valueToTree(jsonPathResult.get(0)));
  }

  private JsonNode getJsonDocumentByFile(GraphQLDirective jsonDirective) {
    GraphQLArgument fileName = jsonDirective.getArgument(JsonDirectives.ARGS_FILE);

    return jsonDataService.getJsonSourceData(fileName.getValue()
        .toString());
  }

  private List<Filter> createJsonPathWithArguments(Map<String, Object> arguments,
      GraphQLFieldDefinition fieldDefinition) {
    List<Pair<String, Object>> predicateFilters = fieldDefinition.getArguments()
        .stream()
        .filter(argument -> argument.getDirective(PredicateDirectives.PREDICATE_NAME) != null)
        .map(argument -> getArgumentsPair(arguments, argument))
        .filter(pair -> pair.getLeft() != null && pair.getRight() != null)
        .collect(Collectors.toList());

    return predicateFilters.stream()
        .map(predicateFilter -> where(predicateFilter.getLeft()).is(predicateFilter.getRight()))
        .map(Filter::filter)
        .collect(Collectors.toList());
  }

  private Pair<String, Object> getArgumentsPair(Map<String, Object> arguments, GraphQLArgument argument) {
    GraphQLDirective predicate = argument.getDirective(PredicateDirectives.PREDICATE_NAME);

    String key = Optional.ofNullable(predicate.getArgument(PredicateDirectives.ARGS_PROPERTY))
        .map(GraphQLArgument::getValue)
        .map(Object::toString)
        .orElse(argument.getName());

    return Pair.of(key, arguments.get(argument.getName()));
  }

}
