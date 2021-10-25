package org.dotwebstack.framework.backend.postgres.query;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.backend.BackendConstants.PAGING_KEY_PREFIX;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.jooq.Record;
import org.jooq.SelectQuery;

@Accessors(fluent = true)
@Setter
class PagingBuilder {

  @NotNull
  private RequestContext requestContext;

  @NotNull
  private SelectQuery<Record> dataQuery;

  private PagingBuilder() {}

  static PagingBuilder newPaging() {
    return new PagingBuilder();
  }

  void build() {
    addPagingCriteria();
  }

  private void addPagingCriteria() {
    var source = requestContext.getSource();

    if (source == null) {
      return;
    }

    Optional<Integer> offset =
        ofNullable(source.get(PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME))).map(Integer.class::cast);
    Optional<Integer> first =
        ofNullable(source.get(PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME))).map(Integer.class::cast);

    if (offset.isPresent() && first.isPresent()) {
      dataQuery.addLimit(offset.get(), first.get());
    }
  }
}
