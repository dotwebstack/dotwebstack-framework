package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Getter;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

@Builder
@Getter
class SelectWrapper {

  private final SelectJoinStep<Record> query;

  private final UnaryOperator<Map<String, Object>> rowAssembler;
}
