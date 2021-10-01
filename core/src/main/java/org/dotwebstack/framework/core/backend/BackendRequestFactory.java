package org.dotwebstack.framework.core.backend;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.springframework.stereotype.Component;

@Component
public class BackendRequestFactory {

  private final Schema schema;

  public BackendRequestFactory(Schema schema) {
    this.schema = schema;
  }

  public CollectionRequest createCollectionRequest(DataFetchingEnvironment environment) {
    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(environment))
        .build();
  }

  public ObjectRequest createObjectRequest(DataFetchingEnvironment environment) {
    var selectionSet = environment.getSelectionSet();

    return ObjectRequest.builder()
        .selectedScalarFields(getScalarFields(selectionSet.getImmediateFields()))
        .build();
  }

  private List<SelectedField> getScalarFields(List<SelectedField> selectedFields) {
    return selectedFields.stream()
        .filter(BackendRequestFactory::isScalarField)
        .collect(Collectors.toList());
  }

  // TODO move to utils class
  private static boolean isScalarField(SelectedField selectedField) {
    var unwrappedType = GraphQLTypeUtil.unwrapAll(selectedField.getType());
    return unwrappedType instanceof GraphQLScalarType;
  }
}
