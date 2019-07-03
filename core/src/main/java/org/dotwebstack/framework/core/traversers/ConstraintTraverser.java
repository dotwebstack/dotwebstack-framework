package org.dotwebstack.framework.core.traversers;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConstraintTraverser extends CoreTraverser {

  public Map<GraphQLDirectiveContainer, Object> getInputObjectDirectiveContainers(
      DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String, Object> flattenedArguments = TraverserHelper.flattenArguments(dataFetchingEnvironment.getArguments());

    return fieldDefinition.getArguments()
        .stream()
        .flatMap(argument -> getInputObjectFieldsFromArgument(argument).stream())
        .filter(directiveContainer -> directiveContainer.getDirective(CoreDirectives.CONSTRAINT_NAME) != null)
        .map(directiveContainer -> new AbstractMap.SimpleEntry<>(directiveContainer,
            flattenedArguments.get(directiveContainer.getName())))
        .filter(entry -> entry.getValue() != null)
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }
}
