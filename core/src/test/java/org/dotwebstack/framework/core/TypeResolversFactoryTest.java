package org.dotwebstack.framework.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.TypeResolutionEnvironment;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.junit.jupiter.api.Test;

class TypeResolversFactoryTest {

  @Test
  void typeResolver_createResolvers_whenInterfacesAreConfigured() {
    var configFile = "dotwebstack/dotwebstack-objecttypes-with-interfaces.yaml";
    var typeResolvers = getTypeResolvers(configFile);

    assertThat(typeResolvers.size(), is(2));
    assertThat(typeResolvers.get("BaseObject"), notNullValue());
  }

  @Test
  void typeResolver_doNotCreateResolvers_whenNoInterfacesAreConfigured() {
    var configFile = "dotwebstack/dotwebstack-objecttypes.yaml";
    var typeResolvers = getTypeResolvers(configFile);

    assertThat(typeResolvers.size(), is(0));
  }

  @Test
  void typeResolver_resolveType_whenTypeExists() {
    var configFile = "dotwebstack/dotwebstack-objecttypes-with-interfaces.yaml";
    var typeResolvers = getTypeResolvers(configFile);
    var typeResolutionEnvironment = getTypeResolutionEnvironment(configFile, Map.of("dtype", "Brewery"));

    var graphqlObjectType = typeResolvers.get("BaseObject")
        .getType(typeResolutionEnvironment);
    assertThat(graphqlObjectType, notNullValue());
    assertThat(graphqlObjectType.getName(), is("Brewery"));
  }

  @Test
  void typeResolver_resolveType_withUnknownDtype() {
    var configFile = "dotwebstack/dotwebstack-objecttypes-with-interfaces.yaml";
    var typeResolvers = getTypeResolvers(configFile);
    var typeResolutionEnvironment = getTypeResolutionEnvironment(configFile, Map.of("dtype", "DoesNotExist"));
    var graphqlObjectType = typeResolvers.get("BaseObject")
        .getType(typeResolutionEnvironment);
    assertNull(graphqlObjectType);
  }

  @Test
  void typeResolver_resolveType_whenDtypeIsMissing() {
    var configFile = "dotwebstack/dotwebstack-objecttypes-with-interfaces.yaml";
    var typeResolvers = getTypeResolvers(configFile);
    var typeResolutionEnvironment = getTypeResolutionEnvironment(configFile, Map.of("wrong", "DoesNotExist"));
    var graphqlObjectType = typeResolvers.get("BaseObject")
        .getType(typeResolutionEnvironment);
    assertNull(graphqlObjectType);
  }

  private Map<String, TypeResolver> getTypeResolvers(String pathToConfigFile) {
    var dotWebStackConfiguration = TestHelper.loadSchemaWithDefaultBackendModule(pathToConfigFile);
    return new TypeResolversFactory(dotWebStackConfiguration).createTypeResolvers();
  }

  private TypeResolutionEnvironment getTypeResolutionEnvironment(String pathToConfigFile,
      Map<String, String> envObjects) {
    var dotWebStackConfiguration = TestHelper.loadSchemaWithDefaultBackendModule(pathToConfigFile);
    var typeResolvers = new TypeResolversFactory(dotWebStackConfiguration).createTypeResolvers();
    var typeDefinitionRegistry =
        new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of()).createTypeDefinitionRegistry();

    var typeResolutionEnvironment = mock(TypeResolutionEnvironment.class);
    when(typeResolutionEnvironment.getObject()).thenReturn(envObjects);
    var runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
    typeResolvers.forEach((interfaceName, resolver) -> runtimeWiringBuilder.type(interfaceName,
        typeWriting -> typeWriting.typeResolver(resolver)));
    when(typeResolutionEnvironment.getSchema())
        .thenReturn(TestHelper.schemaToGraphQl(typeDefinitionRegistry, runtimeWiringBuilder.build()));

    return typeResolutionEnvironment;
  }
}
