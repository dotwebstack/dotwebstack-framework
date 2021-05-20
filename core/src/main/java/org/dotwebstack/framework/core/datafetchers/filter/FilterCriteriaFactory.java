package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLSchemaElement;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.springframework.stereotype.Component;

@Component
public class FilterCriteriaFactory {

  public List<FilterCriteria> getFilterCriterias(TypeConfiguration<?> typeConfiguration,
      GraphQLInputObjectType inputObjectType, Map<String, Object> data) {
    for (GraphQLSchemaElement child : inputObjectType.getChildren()) {
      if (child instanceof GraphQLInputObjectField) {
        GraphQLInputObjectField inputObjectField = (GraphQLInputObjectField) child;
        Map<String, Object> childData = (Map<String, Object>) data.get(inputObjectField.getName());

        FilterConfiguration filterConfiguration = typeConfiguration.getFilters()
            .get(((GraphQLInputObjectField) child).getName());

        AbstractFieldConfiguration fieldConfiguration = typeConfiguration.getFields()
            .get(filterConfiguration.getField());

        // inputObjectField == StringFilter or IntFilter etc

        for (GraphQLSchemaElement child2 : inputObjectField.getChildren()) {
          if (child2 instanceof GraphQLInputObjectType) {
            for (GraphQLSchemaElement child3 : child2.getChildren()) {
              GraphQLInputObjectField operatorField = (GraphQLInputObjectField) child3;

              switch (operatorField.getName()) {
                case "eq":
                  return List.of(EqualsFilterCriteria.builder()
                      .field(fieldConfiguration)
                      .value(childData.get(operatorField.getName()))
                      .build());
                default:
                  throw ExceptionHelper.illegalStateException("ble");
              }
            }
            System.out.println();
          }
        }
      }

      throw illegalStateException("Unknown child type!");
    }

    return List.of();
  }

  private FilterCriteria createEqualsFilterCriteria(FieldConfiguration fieldConfiguration, Map<String, Object> child) {
    System.out.println();

    return null;
  }
}
