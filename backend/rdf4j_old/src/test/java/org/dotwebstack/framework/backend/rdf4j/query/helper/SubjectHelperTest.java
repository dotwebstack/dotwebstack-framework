package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLFieldDefinition;
import java.util.List;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final String DWS_BEER_PREFIX = "https://github.com/dotwebstack/beer/def#";

  private static final IRI beerIri = VF.createIRI(DWS_BEER_PREFIX + "beer");

  @Mock
  private GraphQLFieldDefinition breweryFieldMock;

  @Mock
  private GraphQLFieldDefinition beerFieldMock;

  @Mock
  private NodeShape nodeShapeMock;

  @Mock
  private PropertyShape propertyShapeMock;

  @Mock
  private Variable variableMock;

  @Mock
  private Variable variableMock2;

  @Test
  void getSubjectForField_ReturnsSubject_ForSingleTonFieldPath() {
    // Arrange
    Edge edge = Edge.builder()
        .object(Vertice.builder()
            .subject(variableMock)
            .build())
        .build();

    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(breweryFieldMock))
        .build();

    // Act
    Variable subject = SubjectHelper.getSubjectForField(edge, nodeShapeMock, fieldPath);

    // Assert
    assertThat(subject, is(equalTo(variableMock)));
  }

  @Test
  void getSubjectForField_ThrowsError_ForNotExistingEdge() {
    // Arrange
    when(nodeShapeMock.getPropertyShape(any())).thenReturn(propertyShapeMock);
    when(propertyShapeMock.getPath()).thenReturn(PredicatePath.builder()
        .iri(beerIri)
        .build());

    Edge edge = Edge.builder()
        .object(Vertice.builder()
            .subject(variableMock)
            .build())
        .build();

    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(breweryFieldMock, beerFieldMock))
        .build();

    // Act & Assert
    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> SubjectHelper.getSubjectForField(edge, nodeShapeMock, fieldPath));
    assertThat(exception.getMessage(), containsString("Did not find a predicate with name"));
  }

  @Test
  void getSubjectForField_ReturnsSubject_ForNestedEdge() {
    // Arrange
    when(nodeShapeMock.getPropertyShape(any())).thenReturn(propertyShapeMock);
    PredicatePath predicatePath = PredicatePath.builder()
        .iri(beerIri)
        .build();
    when(propertyShapeMock.getPath()).thenReturn(predicatePath);

    Edge edge = Edge.builder()
        .object(Vertice.builder()
            .subject(variableMock)
            .edges(List.of(Edge.builder()
                .predicate(predicatePath.toPredicate())
                .object(Vertice.builder()
                    .subject(variableMock2)
                    .build())
                .build()))
            .build())
        .build();

    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(breweryFieldMock, beerFieldMock))
        .build();

    // Act
    Variable subject = SubjectHelper.getSubjectForField(edge, nodeShapeMock, fieldPath);
    assertThat(subject, is(equalTo(variableMock2)));
  }

}
