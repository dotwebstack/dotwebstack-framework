package org.dotwebstack.framework.core;

import static org.junit.jupiter.api.Assertions.*;

import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.junit.jupiter.api.BeforeEach;

class TypeResolversFactoryTest {

  private SchemaReader schemaReader;

  @BeforeEach
  void doBefore() {
    schemaReader = new SchemaReader(TestHelper.createSimpleObjectMapper());
  }
}
