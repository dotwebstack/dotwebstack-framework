package org.dotwebstack.framework.core.backend;

import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;

public interface BackendDefinition {

  Class<? extends AbstractTypeConfiguration<?>> getTypeConfigurationClass();
}
