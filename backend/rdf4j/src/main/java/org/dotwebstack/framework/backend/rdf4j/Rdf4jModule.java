package org.dotwebstack.framework.backend.rdf4j;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Collections;
import org.dotwebstack.framework.backend.rdf4j.serializers.Rdf4jStringSerializer;
import org.springframework.stereotype.Component;

@Component
public class Rdf4jModule extends SimpleModule {

  static final long serialVersionUID = 1L;

  private static final String MODULE_NAME = "Rdf4jModule";

  private static final String GROUP_ID = "org.dotwebstack.framework";

  private static final String ARTIFACT_ID = "backend-rdf4j";

  public Rdf4jModule() {
    super(MODULE_NAME, new Version(1, 0, 0, null, GROUP_ID, ARTIFACT_ID),
        Collections.singletonList(new Rdf4jStringSerializer()));
  }
}
