package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

public class Assert {

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void assertThat(Object object, Matcher<?> matcher) {
    MatcherAssert.assertThat(object, (Matcher) matcher);
  }
}
