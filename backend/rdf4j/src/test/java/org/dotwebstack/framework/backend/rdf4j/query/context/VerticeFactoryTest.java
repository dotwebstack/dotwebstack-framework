// package org.dotwebstack.framework.backend.rdf4j.query.context;
//
// import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_3;
// import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_LABEL;
// import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_NAME_FIELD;
// import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_TARGET_CLASS;
// import static org.dotwebstack.framework.backend.rdf4j.Constants.SHACL_LITERAL;
// import static org.dotwebstack.framework.backend.rdf4j.Constants.XSD_STRING;
// import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
// import static org.hamcrest.CoreMatchers.is;
// import static org.hamcrest.MatcherAssert.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
//
// import com.google.common.collect.ImmutableList;
// import com.google.common.collect.ImmutableMap;
// import graphql.Scalars;
// import graphql.schema.GraphQLArgument;
// import graphql.schema.GraphQLDirective;
// import graphql.schema.SelectedField;
// import java.util.Collections;
// import org.dotwebstack.framework.backend.rdf4j.serializers.LocalDateSerializer;
// import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
// import org.dotwebstack.framework.backend.rdf4j.serializers.ZonedDateTimeSerializer;
// import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
// import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
// import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
// import org.dotwebstack.framework.core.directives.CoreDirectives;
// import org.dotwebstack.framework.core.directives.FilterOperator;
// import org.dotwebstack.framework.core.traversers.DirectiveContainerTuple;
// import org.eclipse.rdf4j.model.IRI;
// import org.eclipse.rdf4j.model.vocabulary.RDF;
// import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
// import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
// import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
// import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// public class VerticeFactoryTest {
//
// @Mock
// private NodeShape nodeShape;
//
// SerializerRouter router =
// new SerializerRouter(ImmutableList.of(new LocalDateSerializer(), new ZonedDateTimeSerializer()));
//
// SelectVerticeFactory selectVerticeFactory = new SelectVerticeFactory(router);
//
// ConstructVerticeFactory constructVerticeFactory = new ConstructVerticeFactory(router);
//
// @Mock
// SelectedField selectedField;
//
// @Test
// void get_ReturnVertice_ForSimpleBreweryNodeShape() {
// // Arrange
// when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
// SelectQuery query = Queries.SELECT();
//
// // Act
// Vertice vertice = selectVerticeFactory.createVertice(query.var(), query, nodeShape,
// Collections.emptyList(),
// Collections.emptyList());
//
// // Assert
// assertThat(vertice.getEdges()
// .size(), is(1));
// Edge edge = vertice.getEdges()
// .get(0);
//
// assertThat(edge.getPredicate()
// .getQueryString(), is(stringify(RDF.TYPE)));
// assertThat(edge.getObject()
// .getIris()
// .iterator()
// .next()
// .getQueryString(), is(stringify(BREWERY_TARGET_CLASS)));
// }
//
// @Test
// void get_ReturnVertice_WithSortedSelectQuery() {
// // Arrange
// PropertyShape breweryName = PropertyShape.builder()
// .name(BREWERY_NAME_FIELD)
// .path(PredicatePath.builder()
// .iri(BREWERY_LABEL)
// .build())
// .nodeKind(SHACL_LITERAL)
// .datatype(XSD_STRING)
// .build();
//
// when(nodeShape.getPropertyShape(any())).thenReturn(breweryName);
// when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
// SelectQuery query = Queries.SELECT();
//
// // Act
// Vertice vertice = selectVerticeFactory.createVertice(query.var(), query, nodeShape,
// Collections.emptyList(),
// ImmutableList.of(ImmutableMap.of("field", "name", "order", "DESC")));
//
// // Assert
// assertThat(vertice.getOrderables()
// .size(), is(1));
// Orderable orderable = vertice.getOrderables()
// .get(0);
//
// assertThat(orderable.getQueryString(), is("DESC( ?x1 )"));
// }
//
// @Test
// void get_ReturnVertice_WithFilteredSelectQuery() {
// // Arrange
// PropertyShape breweryName = PropertyShape.builder()
// .name(BREWERY_NAME_FIELD)
// .path(PredicatePath.builder()
// .iri(BREWERY_LABEL)
// .build())
// .nodeKind(SHACL_LITERAL)
// .datatype(XSD_STRING)
// .build();
//
// when(nodeShape.getPropertyShape(any())).thenReturn(breweryName);
// when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
// SelectQuery query = Queries.SELECT();
//
// // Act
// Vertice vertice = selectVerticeFactory.createVertice(query.var(), query, nodeShape,
// ImmutableList.of(DirectiveContainerTuple.builder()
// .container(GraphQLArgument.newArgument()
// .name("name")
// .withDirective(GraphQLDirective.newDirective()
// .name(CoreDirectives.FILTER_NAME)
// .argument(GraphQLArgument.newArgument()
// .name(CoreDirectives.FILTER_ARG_FIELD)
// .type(Scalars.GraphQLString)
// .build())
// .argument(GraphQLArgument.newArgument()
// .name(CoreDirectives.FILTER_ARG_OPERATOR)
// .type(Scalars.GraphQLString)
// .build()))
// .type(Scalars.GraphQLString)
// .build())
// .value("Alfa Brouwerij")
// .build()),
// Collections.emptyList());
//
// // Assert
// assertThat(vertice.getEdges()
// .size(), is(2));
// Edge edge = vertice.getEdges()
// .get(1);
//
// assertThat(edge.getObject()
// .getFilters()
// .size(), is(1));
// Filter filter = edge.getObject()
// .getFilters()
// .get(0);
//
// assertThat(filter.getOperator(), is(FilterOperator.EQ));
// assertThat(filter.getOperands()
// .size(), is(1));
//
// Operand operand = filter.getOperands()
// .get(0);
// assertThat(operand.getQueryString(), is("\"Alfa
// Brouwerij\"^^<http://www.w3.org/2001/XMLSchema#string>"));
// }
//
// @Test
// void get_ReturnVertice_WithConstructQuery() {
// // Arrange
// PropertyShape breweryName = PropertyShape.builder()
// .name(BREWERY_NAME_FIELD)
// .path(PredicatePath.builder()
// .iri(BREWERY_LABEL)
// .build())
// .nodeKind(SHACL_LITERAL)
// .datatype(XSD_STRING)
// .build();
//
// when(nodeShape.getPropertyShape(BREWERY_NAME_FIELD)).thenReturn(breweryName);
// when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
// SelectQuery query = Queries.SELECT();
//
// ImmutableList<IRI> subjects = ImmutableList.of(BEER_3);
//
// when(selectedField.getName()).thenReturn(BREWERY_NAME_FIELD);
// when(selectedField.getQualifiedName()).thenReturn(BREWERY_NAME_FIELD);
// ImmutableList<SelectedField> fields = ImmutableList.of(selectedField);
//
// // Act
// Vertice vertice = constructVerticeFactory.createVertice(subjects, query.var(), query, nodeShape,
// fields);
//
// // Assert
// assertThat(vertice.getEdges()
// .size(), is(2));
// Edge edge = vertice.getEdges()
// .get(0);
//
// assertThat(edge.getPredicate()
// .getQueryString(), is(stringify(BREWERY_LABEL)));
// }
// }
