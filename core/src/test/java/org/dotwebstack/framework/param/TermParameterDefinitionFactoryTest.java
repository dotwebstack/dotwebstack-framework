package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.dotwebstack.framework.vocabulary.SHACL;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TermParameterDefinitionFactoryTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private ParameterDefinitionFactory parameterDefinitionFactory;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    parameterDefinitionFactory = new TermParameterDefinitionFactory();
  }

  @Test
  public void supports_ReturnsTrue_ForSupportedIri() {
    // Assert
    boolean result = parameterDefinitionFactory.supports(ELMO.TERM_FILTER);

    // Act
    assertThat(result, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForUnsupportedIri() {
    // Assert
    boolean result = parameterDefinitionFactory.supports(
        VALUE_FACTORY.createIRI("http://unsupported#", "Filter"));

    // Act
    assertThat(result, is(false));
  }

  @Test
  public void create_createsTermParameterDefinition_ForStringShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.STRING);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

  @Test
  public void create_createsTermParameterDefinition_ForIntegerShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.INTEGER);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

  @Test
  public void create_createsTermParameterDefinition_ForBooleanShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.BOOLEAN);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

  @Test
  public void create_createsTermParameterDefinition_ForIriShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.ANYURI);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

  @Test
  public void create_ThrowsConfigurationException_ForMissingShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE);

    Model model = builder.build();

    // Assert
    exception.expect(ConfigurationException.class);
    // XXX (PvH) Vergeet ook niet de message te testen. Nu is elke ConfigurationException valide.

    // Act
    parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);
  }

  @Test
  public void create_createsTermParameterDefinition_WithNullDefaultValue() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.STRING);

    Model model = builder.build();

    Map<String, String> parameterValues = ImmutableMap.of();

    // Act
    TermParameterDefinition result =
        (TermParameterDefinition) parameterDefinitionFactory.create(model,
            DBEERPEDIA.NAME_PARAMETER_ID);

    // XXX (PvH) Is het niet mooier om de shape te exposen op de definition, zodat je dit kan doen:
    // assertThat(result.getShaclShape(), is(PropertyShape.of(...));

    // ...dan hebben we de code die hierna volgt niet meer nodig

    Parameter optionalParameter = result.createOptionalParameter();
    Object handle = optionalParameter.handle(parameterValues);
    // Assert

    // XXX (PvH) Dit is overigens altijd waar
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(handle, is(nullValue()));
  }

  @Test
  public void create_createsTermParameterDefinition_WithProvidedDefaultValue() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.STRING).add(SHACL.DEFAULT_VALUE, "foo");

    Model model = builder.build();

    // Act
    TermParameterDefinition result =
        (TermParameterDefinition) parameterDefinitionFactory.create(model,
            DBEERPEDIA.NAME_PARAMETER_ID);

    // XXX (PvH) Zie comments vorige test

    Parameter optionalParameter = result.createOptionalParameter();
    Object handle = optionalParameter.handle(ImmutableMap.of()); // empty map

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(handle, is("foo"));
  }

}
