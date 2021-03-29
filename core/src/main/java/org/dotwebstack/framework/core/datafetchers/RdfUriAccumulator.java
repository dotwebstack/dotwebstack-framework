package org.dotwebstack.framework.core.datafetchers;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.datafetchers.FieldConstants.RDF_URI_FIELD;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RdfUriAccumulator {

  @NonNull
  private DotWebStackConfiguration dotWebStackConfiguration;

  public Map<String, Object> accumulate(Map<String, Object> data, TypeConfiguration<?> typeConfiguration,
      DataFetchingFieldSelectionSet selectionSet) {
    return accumulate(data, "", typeConfiguration, selectionSet);
  }

  private Map<String, Object> accumulate(Map<String, Object> data, String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingFieldSelectionSet selectionSet) {
    if (selectionSet.contains(fieldPathPrefix + RDF_URI_FIELD)) {
      addRdfUri(data, typeConfiguration);
    }

    data.entrySet()
        .stream()
        .forEach(entry -> {
          String fieldName = entry.getKey();
          Optional<TypeConfiguration<?>> optionalCurrentTypeConfiguration =
              getTypeConfiguration(fieldName, selectionSet);
          if (optionalCurrentTypeConfiguration.isPresent() && entry.getValue() != null) {
            // this is an objecttype, so the data is stored in a Map<String, Object>
            Map<String, Object> objectTypeData = convertObjectTypeDataToMap(entry.getValue());
            final String prefix = fieldPathPrefix.concat(fieldName + "/");
            accumulate(objectTypeData, prefix, optionalCurrentTypeConfiguration.get(), selectionSet);
          }
        });

    return data;
  }

  private Map<String, Object> addRdfUri(Map<String, Object> data, TypeConfiguration<?> currentTypeConfiguration) {
    String uriTemplate = currentTypeConfiguration.getUriTemplate();
    if (!StringUtils.isBlank(uriTemplate)) {
      StringSubstitutor substitutor = new StringSubstitutor(data);
      String rdfUri = substitutor.replace(uriTemplate);
      data.put(RDF_URI_FIELD, rdfUri);
    }

    return data;
  }

  /**
   * Converts the data of an ObjectType to a Map.
   *
   * @param data the data to be casted
   * @return the data as a map
   */
  @SuppressWarnings(value = "unchecked")
  private Map<String, Object> convertObjectTypeDataToMap(Object data) {
    if (Map.class.isAssignableFrom(data.getClass())) {
      return (Map<String, Object>) data;
    }
    throw new IllegalArgumentException("Object cannot be converted to a map");
  }

  private Optional<TypeConfiguration<?>> getTypeConfiguration(String fieldName,
      DataFetchingFieldSelectionSet selectionSet) {
    if (selectionSet.contains(fieldName)) {
      GraphQLOutputType qlOutputType = selectionSet.getFields(fieldName)
          .get(0)
          .getFieldDefinition()
          .getType();

      return getTypeConfiguration(qlOutputType);
    }

    return Optional.empty();
  }

  private Optional<TypeConfiguration<?>> getTypeConfiguration(GraphQLOutputType outputType) {
    GraphQLType nullableType = GraphQLTypeUtil.unwrapNonNull(outputType);
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(nullableType);

    return ofNullable(dotWebStackConfiguration.getTypeMapping()
        .get(rawType.getName()));
  }

}
