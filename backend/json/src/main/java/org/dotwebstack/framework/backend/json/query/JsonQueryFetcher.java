package org.dotwebstack.framework.backend.json.query;

import static com.jayway.jsonpath.Criteria.where;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  private final ObjectMapper jsonMapper = new ObjectMapper();

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

    Stream<JsonSolution> stream = jsonPathResult.stream()
        .flatMap(subject -> {
          if (subject instanceof JSONArray) {
            return ((JSONArray) subject).stream();
          }
          return Stream.of(subject);
        })
        .map(jsonMapper::valueToTree)
        .map(JsonNode.class::cast)
        .peek(subject -> excludeFields(jsonDirective, subject))
        .map(JsonSolution::new);

    if (GraphQLTypeUtil.isList(outputType)) {
      return stream.collect(Collectors.toList());
    }

    return stream.findFirst()
        .orElseThrow();
  }

  private void excludeFields(GraphQLDirective jsonDirective, JsonNode subject) {
    if (!(subject instanceof ObjectNode)) {
      return;
    }

    ofNullable(jsonDirective.getArgument(JsonDirectives.ARGS_EXCLUDE))
        .filter(argument -> Objects.nonNull(argument.getValue()))
        .ifPresent(argument -> excludeField(subject, argument));
  }

  @SuppressWarnings("unchecked")
  private void excludeField(JsonNode subject, GraphQLArgument argument) {
    ((Collection<String>) argument.getValue()).forEach(value -> {
      ObjectNode objectNode = (ObjectNode) subject.findParent(value);
      if (objectNode != null) {
        objectNode.remove(value);
      }
    });
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

    String key = ofNullable(predicate.getArgument(PredicateDirectives.ARGS_PROPERTY)).map(GraphQLArgument::getValue)
        .map(Object::toString)
        .orElse(argument.getName());

    return Pair.of(key, arguments.get(argument.getName()));
  }

}
