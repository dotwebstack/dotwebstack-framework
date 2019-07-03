package org.dotwebstack.framework.core.traversers;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FilterDirectiveTraverser extends CoreTraverser {

  public Map<GraphQLDirectiveContainer, Object> getInputObjectDirectiveContainers(
      DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String, Object> flattenedArguments = TraverserHelper.flattenArguments(dataFetchingEnvironment.getArguments());

    return fieldDefinition.getArguments()
        .stream()
        .flatMap(argument -> getInputObjectFieldsFromArgument(argument).stream())
        .filter(directiveContainer -> directiveContainer.getDirective(CoreDirectives.FILTER_NAME) != null)
        .map(directiveContainer -> new AbstractMap.SimpleEntry<>(directiveContainer,
            flattenedArguments.get(directiveContainer.getName())))
        .filter(entry -> entry.getValue() != null)
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  public Map<GraphQLDirectiveContainer, Object> getObjectDirectiveContainers(
      DataFetchingEnvironment dataFetchingEnvironment) {
    return getObjectTypes(dataFetchingEnvironment).entrySet()
        .stream()
        .filter(entry -> entry.getKey()
            .getDirective(CoreDirectives.FILTER_NAME) != null)
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }
}
