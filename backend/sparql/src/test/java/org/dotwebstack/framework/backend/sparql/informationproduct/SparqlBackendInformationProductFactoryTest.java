package org.dotwebstack.framework.backend.sparql.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
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
  private QueryEvaluator queryEvaluatorMock;

  @Mock
  private TemplateProcessor templateProcessorMock;

  @Mock
  private SparqlBackend backendMock;

  private SparqlBackendInformationProductFactory informationProductFactory;

  @Before
  public void setUp() {
    informationProductFactory =
        new SparqlBackendInformationProductFactory(queryEvaluatorMock, templateProcessorMock);
  }

  @Test
  public void constructor_ThrowsException_WithMissingQueryEvaluator() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProductFactory(null, templateProcessorMock);
  }

  @Test
  public void create_InformationProductIsCreated_WithValidData() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.SELECT_ALL_QUERY).build();

    // Act
    Parameter<?> parameter1Mock = mock(Parameter.class);
    Parameter<?> parameter2Mock = mock(Parameter.class);
    Parameter<?> parameter3Mock = mock(Parameter.class);

    Collection<Parameter> parameters =
        ImmutableList.of(parameter1Mock, parameter2Mock, parameter3Mock);

    InformationProduct result =
        informationProductFactory.create(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
            DBEERPEDIA.BREWERIES_LABEL.stringValue(), backendMock, parameters, statements);

    // Assert
    assertThat(result, instanceOf(SparqlBackendInformationProduct.class));
    assertThat(((SparqlBackendInformationProduct) result).getQuery(),
        equalTo(DBEERPEDIA.SELECT_ALL_QUERY.stringValue()));

    assertThat(((SparqlBackendInformationProduct) result).getParameters(),
        contains(parameter1Mock, parameter2Mock, parameter3Mock));
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
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), backendMock, ImmutableList.of(), statements);
  }

  @Test
  public void create_DeterminesTupleQueryType_ForSelectQuery() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.SELECT_ALL_QUERY).add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.RESULT_TYPE,
            SimpleValueFactory.getInstance().createIRI(ELMO.RESULT_TYPE.getNamespace(),
                ResultType.TUPLE.name())).build();

    // Act
    InformationProduct result =
        informationProductFactory.create(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
            DBEERPEDIA.BREWERIES_LABEL.stringValue(), backendMock, ImmutableList.of(), statements);

    // Assert
    assertThat(result.getResultType(), equalTo(ResultType.TUPLE));
  }

  @Test
  public void create_DeterminesDefaultQueryType_ForSelectQuery() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.SELECT_ALL_QUERY).build();

    // Act
    InformationProduct result =
        informationProductFactory.create(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
            DBEERPEDIA.BREWERIES_LABEL.stringValue(), backendMock, ImmutableList.of(), statements);

    // Assert
    assertThat(result.getResultType(), equalTo(ResultType.GRAPH));
  }


  @Test
  public void create_DeterminesGraphQueryType_ForConstructQuery() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.QUERY,
        DBEERPEDIA.CONSTRUCT_ALL_QUERY).build();

    // Act
    InformationProduct result =
        informationProductFactory.create(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
            DBEERPEDIA.BREWERIES_LABEL.stringValue(), backendMock, ImmutableList.of(), statements);

    // Assert
    assertThat(result.getResultType(), equalTo(ResultType.GRAPH));
  }
}
