package org.dotwebstack.framework.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeResolversFactoryTest {

  private SchemaReader schemaReader;

  @BeforeEach
  void doBefore() {
    schemaReader = new SchemaReader(TestHelper.createSimpleObjectMapper());
  }
//TODO: FIXME
//  @Test
//  void typeResolver_createResolvers_whenInterfacesAreConfigured() {
//    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-objecttypes-with-interfaces.yaml");
//    var typeResolvers = new TypeResolversFactory(dotWebStackConfiguration).createTypeResolvers();
//
//    assertThat(typeResolvers.size(), is(2));
//    assertThat(typeResolvers.get("Organization"), notNullValue());
//    assertThat(typeResolvers.get("Object"), notNullValue());
//  }

//  @Test
//  void typeResolver_doNotCreateResolvers_whenNoInterfacesAreConfigured() {
//    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-objecttypes.yaml");
//    var typeResolvers = new TypeResolversFactory(dotWebStackConfiguration).createTypeResolvers();
//
//    assertThat(typeResolvers.size(), is(0));
//  }
}
