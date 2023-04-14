package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

class PagingBuilderTest {

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  private static final String OFFSET_KEY = "$paging:offset";

  private static final String FIRST_KEY = "$paging:first";

  @Test
  void build_addLimit_forPaging() {
    RequestContext context = RequestContext.builder()
        .source(Map.of(OFFSET_KEY, 1, FIRST_KEY, 10))
        .build();

    SelectQuery<Record> dataQuery = dslContext.selectQuery();

    PagingBuilder.newPaging()
        .requestContext(context)
        .dataQuery(dataQuery)
        .build();

    assertThat(dataQuery.getSQL(), is("select 1 offset ? rows fetch next ? rows only"));
    assertThat(dataQuery.getBindValues()
        .get(0), is(1L));
    assertThat(dataQuery.getBindValues()
        .get(1), is(10L));
  }

  @Test
  void build_noLimitAddition_forNullSource() {
    RequestContext context = RequestContext.builder()
        .source(null)
        .build();

    testNoLimitAddition(context);
  }

  @Test
  void build_noLimitAddition_forMissingLimit() {
    RequestContext context = RequestContext.builder()
        .source(Map.of(FIRST_KEY, 10))
        .build();

    testNoLimitAddition(context);
  }

  @Test
  void build_noLimitAddition_forMissingOffset() {
    RequestContext context = RequestContext.builder()
        .source(Map.of(OFFSET_KEY, 1))
        .build();

    testNoLimitAddition(context);
  }

  private void testNoLimitAddition(RequestContext context) {
    SelectQuery<Record> dataQuery = dslContext.selectQuery();

    PagingBuilder.newPaging()
        .requestContext(context)
        .dataQuery(dataQuery)
        .build();

    assertThat(dataQuery.getSQL(), is("select 1"));
  }

  @Test
  void build_throwsException_forMissingFields() {
    PagingBuilder builder = PagingBuilder.newPaging();
    var exception = assertThrows(ConstraintViolationException.class, builder::build);

    assertThat(exception.getMessage(),
        startsWith("class org.dotwebstack.framework.backend.postgres.query.PagingBuilder has validation errors (2):"));
  }

}
