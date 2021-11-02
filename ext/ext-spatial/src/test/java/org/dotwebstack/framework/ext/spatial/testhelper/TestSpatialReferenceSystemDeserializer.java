package org.dotwebstack.framework.ext.spatial.testhelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;

public class TestSpatialReferenceSystemDeserializer extends JsonDeserializer<SpatialReferenceSystem> {

  private final SpatialBackendModule<TestSpatialReferenceSystem> backendModule = new TestSpatialBackendModule();

  @Override
  public SpatialReferenceSystem deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    return parser.readValueAs(backendModule.getSpatialReferenceSystemClass());
  }
}
