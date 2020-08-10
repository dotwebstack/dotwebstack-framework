package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.model.Constraint;
import org.dotwebstack.framework.backend.rdf4j.query.model.OrderBy;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SortHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Mock
  private GraphQLFieldDefinition fieldDefinition1;

  @Mock
  private GraphQLFieldDefinition fieldDefinition2;

  @Test
  public void findOrderVariable_returnsVariable_forFieldPathLength1() {
    // Arrange
    Vertice vertice = buildVertice();
    NodeShape nodeShape = buildNodeShape();
    OuterQuery<?> query = Queries.CONSTRUCT();
    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(this.fieldDefinition1))
        .build();
    when(this.fieldDefinition1.getName()).thenReturn("field1");
    OrderBy orderBy = OrderBy.builder()
        .order("ASC")
        .fieldPath(fieldPath)
        .build();

    // Act
    Optional<Variable> var = SortHelper.findOrderVariable(vertice, nodeShape, query, orderBy);

    // Assert
    assertThat(var.isPresent(), is(true));
    assertThat(var.get()
        .getQueryString(), is("?x0"));
  }

  @Test
  public void findOrderVariable_returnsVariable_forFieldPathLength2() {
    // Arrange
    Vertice vertice = buildVertice();
    NodeShape nodeShape = buildNodeShape();
    OuterQuery<?> query = Queries.CONSTRUCT();
    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(this.fieldDefinition1, this.fieldDefinition2))
        .build();
    when(this.fieldDefinition1.getName()).thenReturn("field1");
    when(this.fieldDefinition2.getName()).thenReturn("field2");
    OrderBy orderBy = OrderBy.builder()
        .order("ASC")
        .fieldPath(fieldPath)
        .build();

    // Act
    Optional<Variable> var = SortHelper.findOrderVariable(vertice, nodeShape, query, orderBy);

    // Assert
    assertThat(var.isPresent(), is(true));
    assertThat(var.get()
        .getQueryString(), is("?x1"));
  }

  @ParameterizedTest
  @MethodSource
  public void getDefaultOrderByValue_returnsEmptyString_for_nonNumeric(GraphQLScalarType type) {
    // Arrange
    when(fieldDefinition1.getType()).thenReturn(type);

    // Act
    RdfValue value = SortHelper.getDefaultOrderByValue(fieldDefinition1);

    // Assert
    assertThat(value.getQueryString(), is("\"\""));
  }

  private static Stream<GraphQLScalarType> getDefaultOrderByValue_returnsEmptyString_for_nonNumeric() {
    return Stream.of(Scalars.GraphQLString, Scalars.GraphQLBoolean, Scalars.GraphQLChar, Scalars.GraphQLID);
  }

  @ParameterizedTest
  @MethodSource
  public void getDefaultOrderByValue_returnsZero_for_numeric(GraphQLScalarType type) {
    // Arrange
    when(fieldDefinition1.getType()).thenReturn(type);

    // Act
    RdfValue value = SortHelper.getDefaultOrderByValue(fieldDefinition1);

    // Assert
    assertThat(value.getQueryString(), is("0"));
  }

  private static Stream<GraphQLScalarType> getDefaultOrderByValue_returnsZero_for_numeric() {
    return SortHelper.NUMERIC_TYPES.stream();
  }

  private static NodeShape buildNodeShape() {
    PropertyShape propertyShape2 = PropertyShape.builder()
        .name("field2")
        .path(PredicatePath.builder()
            .iri(VF.createIRI("http://www.example.com/iri2"))
            .build())
        .build();

    NodeShape nodeShape2 = NodeShape.builder()
        .propertyShapes(Map.of("field2", propertyShape2))
        .classes(Set.of(Set.of(VF.createIRI("http://www.example.com#testType2"))))
        .build();

    PropertyShape propertyShape1 = PropertyShape.builder()
        .name("field1")
        .node(nodeShape2)
        .path(PredicatePath.builder()
            .iri(VF.createIRI("http://www.example.com/iri1"))
            .build())
        .build();

    return NodeShape.builder()
        .propertyShapes(Map.of("field1", propertyShape1))
        .classes(Set.of(Set.of(VF.createIRI("http://www.example.com#testType"))))
        .build();
  }

  private static Vertice buildVertice() {
    IRI iri = VF.createIRI("http://www.example.com#testType");

    return Vertice.builder()
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .predicate(() -> "<" + RDF.TYPE.stringValue() + ">")
            .values(Set.of(iri))
            .build()))
        .build();
  }
}
