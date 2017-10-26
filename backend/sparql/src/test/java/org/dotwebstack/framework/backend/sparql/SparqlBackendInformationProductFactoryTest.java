package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendInformationProductFactoryTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private SparqlBackend backend;

  private SparqlBackendInformationProductFactory informationProductFactory;

  @Before
  public void setUp() {
    informationProductFactory = new SparqlBackendInformationProductFactory(queryEvaluator);
  }

  @Test
  public void constructor_ThrowsException_WithMissingQueryEvaluator() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProductFactory(null);
  }

  @Test
  public void create_InformationProductIsCreated_WithValidData() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.SELECT_ALL_QUERY).build();

    // Act
    Filter filter1 = mock(Filter.class);
    Filter filter2 = mock(Filter.class);
    Filter filter3 = mock(Filter.class);

    Collection<Filter> requiredFilters = ImmutableList.of(filter1, filter2);
    Collection<Filter> optionalFilters = ImmutableList.of(filter3);

    InformationProduct result = informationProductFactory.create(
        DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, DBEERPEDIA.BREWERIES_LABEL.stringValue(), backend,
        requiredFilters, optionalFilters, statements);

    // Assert
    assertThat(result, instanceOf(SparqlBackendInformationProduct.class));
    assertThat(((SparqlBackendInformationProduct) result).getQuery(),
        equalTo(DBEERPEDIA.SELECT_ALL_QUERY.stringValue()));

    assertThat(((SparqlBackendInformationProduct) result).getFilters(),
        contains(filter1, filter2, filter3));
  }

  @Test
  public void create_ThrowsException_WhenQueryIsMissing() {
    // Arrange
    Model statements = new ModelBuilder().build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("No <%s> statement has been found for a SPARQL information product <%s>.",
            ELMO.QUERY, DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT));

    // Act
    informationProductFactory.create(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), backend, ImmutableList.of(), ImmutableList.of(),
        statements);
  }

  @Test
  public void create_DeterminesTupleQueryType_ForSelectQuery() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.SELECT_ALL_QUERY).build();

    // Act
    InformationProduct result = informationProductFactory.create(
        DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, DBEERPEDIA.BREWERIES_LABEL.stringValue(), backend,
        ImmutableList.of(), ImmutableList.of(), statements);

    // Assert
    assertThat(result.getResultType(), equalTo(ResultType.TUPLE));
  }

  @Test
  public void create_DeterminesGraphQueryType_ForConstructQuery() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.CONSTRUCT_ALL_QUERY).build();

    // Act
    InformationProduct result = informationProductFactory.create(
        DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, DBEERPEDIA.BREWERIES_LABEL.stringValue(), backend,
        ImmutableList.of(), ImmutableList.of(), statements);

    // Assert
    assertThat(result.getResultType(), equalTo(ResultType.GRAPH));
  }

  @Test
  public void create_ThrowsException_ForInvalidQueryType() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.ASK_ALL_QUERY).build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Type of query <ASK WHERE { ?s ?p ?o }> could not be determined. "
        + "Only SELECT and CONSTRUCT are supported.");

    // Act
    informationProductFactory.create(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), backend, ImmutableList.of(), ImmutableList.of(),
        statements);
  }

  @Test
  public void create_ThrowsException_ForMalformedQuery() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.MALFORMED_QUERY).build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format(
        "Type of query <%s> could not be determined. "
            + "Query is a malformed query and cannot be processed: "
            + "Encountered \" <VAR1> \"?s \"\" at line 1, column 11.",
        DBEERPEDIA.MALFORMED_QUERY.stringValue()));

    // Act
    informationProductFactory.create(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), backend, ImmutableList.of(), ImmutableList.of(),
        statements);
  }

}
