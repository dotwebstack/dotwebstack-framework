package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;
import org.jooq.Field;

@Builder
@Getter
class JoinInformation {

  private final Field<Object> parent;

  private final String referencedField;
}