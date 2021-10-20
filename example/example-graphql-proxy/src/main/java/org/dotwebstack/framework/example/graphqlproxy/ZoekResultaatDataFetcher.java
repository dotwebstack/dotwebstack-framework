package org.dotwebstack.framework.example.graphqlproxy;

import graphql.ExecutionInput;
import graphql.language.Argument;
import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import org.dotwebstack.graphql.orchestrate.schema.Subschema;

public class ZoekResultaatDataFetcher implements DataFetcher<CompletionStage<ZoekResultaat>> {

  private final Subschema subschema;

  public ZoekResultaatDataFetcher(Subschema subschema) {
    this.subschema = subschema;
  }

  @Override
  @SuppressWarnings("unchecked")
  public CompletionStage<ZoekResultaat> get(DataFetchingEnvironment environment) {
    var queryString = AstPrinter.printAst(createQuery());

    var executionInput = ExecutionInput.newExecutionInput()
        .query(queryString)
        .build();

    return subschema.execute(executionInput)
        .thenApply(result -> (Map<String, Object>) result.getData())
        .thenApply(data -> ZoekResultaat.builder()
            .naam("oppervlakte")
            .build());
  }

  private Document createQuery() {
    var arguments = List.of(Argument.newArgument("identificatie", StringValue.of("0200010000130331"))
        .build());

    var selectionSet = SelectionSet.newSelectionSet()
        .selection(Field.newField("verblijfsobject")
            .arguments(arguments)
            .selectionSet(SelectionSet.newSelectionSet()
                .selection(Field.newField("oppervlakte")
                    .build())
                .build())
            .build())
        .build();

    var operation = OperationDefinition.newOperationDefinition()
        .operation(OperationDefinition.Operation.QUERY)
        .selectionSet(selectionSet)
        .build();

    return Document.newDocument()
        .definition(operation)
        .build();
  }
}
