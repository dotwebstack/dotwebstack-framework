package org.dotwebstack.framework.backend.postgres.helpers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.junit.jupiter.api.Test;

class CyclicRefDetectorTest {

  @Test
  void isProcessed_returnsBoolean_forObjectTypeAndField() {
    var objectType = createObjectType();
    var objectField = objectType.getField("addressGeometry");

    var cyclicRefDetector = new CyclicRefDetector();
    assertFalse(cyclicRefDetector.isProcessed(objectType, objectField));

    assertTrue(cyclicRefDetector.isProcessed(objectType, objectField));
  }

  private PostgresObjectType createObjectType() {
    var address = new PostgresObjectType();
    address.setFields(createAddressFields(address));

    return address;
  }

  private Map<String, PostgresObjectField> createAddressFields(PostgresObjectType objectType) {
    var location = new PostgresObjectField();
    location.setName("addressGeometry");
    location.setColumn("address_geometry");
    location.setType("Geometry");
    location.setTargetType(objectType);
    return Map.of("addressGeometry", location);
  }
}
