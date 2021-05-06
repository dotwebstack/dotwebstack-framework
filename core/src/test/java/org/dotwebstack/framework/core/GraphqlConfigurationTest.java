package org.dotwebstack.framework.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.TypeUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphqlConfigurationTest {

  private final GraphqlConfiguration graphqlConfiguration = new GraphqlConfiguration();

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Test
  void typeDefinitionRegistry_registersScalarFields_whenConfigured() {
    TypeConfigurationImpl typeConfiguration = mock(TypeConfigurationImpl.class);
    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(Map.of("Brewery", typeConfiguration));

    // LinkedHashMap maintains insertion order
    var fields = new LinkedHashMap<String, FieldConfigurationImpl>();
    fields.put("name", mockField("String", false, false));
    fields.put("abv", mockField("Int", true, false));
    fields.put("aliases", mockField("String", false, true));
    fields.put("ratings", mockField("Float", true, true));
    when(typeConfiguration.getFields()).thenReturn(fields);

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Brewery")
        .isPresent(), is(true));

    var typeDefinition = registry.getType("Brewery")
        .orElseThrow();
    assertThat(typeDefinition.getName(), is("Brewery"));
    assertThat(typeDefinition, instanceOf(ObjectTypeDefinition.class));

    List<FieldDefinition> fieldDefinitions = ((ObjectTypeDefinition) typeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions, Matchers.hasSize(4));

    assertThat(fieldDefinitions.get(0)
        .getName(), is("name"));
    assertNonNullType(fieldDefinitions.get(0)
        .getType(), "String");

    assertThat(fieldDefinitions.get(1)
        .getName(), is("abv"));
    assertType(fieldDefinitions.get(1)
        .getType(), "Int");

    assertThat(fieldDefinitions.get(2)
        .getName(), is("aliases"));
    assertNonNullListType(fieldDefinitions.get(2)
        .getType(), "String");

    assertThat(fieldDefinitions.get(3)
        .getName(), is("ratings"));
    assertListType(fieldDefinitions.get(3)
        .getType(), "Float");
  }

  private FieldConfigurationImpl mockField(String type, boolean nullable, boolean list) {
    FieldConfigurationImpl nameField = mock(FieldConfigurationImpl.class);
    when(nameField.getType()).thenReturn(type);
    when(nameField.isNullable()).thenReturn(nullable);
    when(nameField.isList()).thenReturn(list);

    return nameField;
  }

  private static void assertType(Type<?> type, String typeName) {
    assertThat(type, instanceOf(TypeName.class));
    assertThat(((TypeName) type).getName(), is(typeName));
  }

  private static void assertNonNullType(Type<?> type, String typeName) {
    assertThat(type, instanceOf(NonNullType.class));
    assertType(TypeUtil.unwrapOne(type), typeName);
  }

  private static void assertListType(Type<?> type, String typeName) {
    assertThat(type, instanceOf(ListType.class));
    assertNonNullType(TypeUtil.unwrapOne(type), typeName);
  }

  private static void assertNonNullListType(Type<?> type, String typeName) {
    assertThat(type, instanceOf(NonNullType.class));
    assertListType(TypeUtil.unwrapOne(type), typeName);
  }

  static class TypeConfigurationImpl extends AbstractTypeConfiguration<FieldConfigurationImpl> {

    @Override
    public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
      return null;
    }

    @Override
    public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
      return null;
    }

    @Override
    public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
      return null;
    }
  }

  static class FieldConfigurationImpl extends AbstractFieldConfiguration {
  }
}
