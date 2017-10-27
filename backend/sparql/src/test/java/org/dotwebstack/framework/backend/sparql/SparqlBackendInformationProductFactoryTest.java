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
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
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

  // XXX (PvH) Waar test je waar het ResultType is gezet?
  // XXX (PvH) Ik mis een test voor de default ResultType
  @Test
  public void create_InformationProductIsCreated_WithValidData() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.SELECT_ALL_QUERY).build();

    // Act
    Parameter parameter1 = mock(Parameter.class);
    Parameter parameter2 = mock(Parameter.class);
    Parameter parameter3 = mock(Parameter.class);

    Collection<Parameter> requiredParameters = ImmutableList.of(parameter1, parameter2);
    Collection<Parameter> optionalParameters = ImmutableList.of(parameter3);

    InformationProduct result = informationProductFactory.create(
        DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, DBEERPEDIA.BREWERIES_LABEL.stringValue(), backend,
        requiredParameters, optionalParameters, statements);

    // Assert
    assertThat(result, instanceOf(SparqlBackendInformationProduct.class));
    assertThat(((SparqlBackendInformationProduct) result).getQuery(),
        equalTo(DBEERPEDIA.SELECT_ALL_QUERY.stringValue()));

    assertThat(((SparqlBackendInformationProduct) result).getParameters(),
        contains(parameter1, parameter2, parameter3));
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
        DBEERPEDIA.SELECT_ALL_QUERY).add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.RESULT_TYPE,
            SimpleValueFactory.getInstance().createIRI(ELMO.RESULT_TYPE.getNamespace(),
                ResultType.TUPLE.name())).build();

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
}
