package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Page {
  public static final int DEFAULT_SIZE = 10;

  private final int offset;

  private final int size;

  public static Page pageWithSize(int size) {
    return Page.builder()
        .offset(0)
        .size(size)
        .build();
  }

  public static Page pageWithDefaultSize() {
    return pageWithSize(DEFAULT_SIZE);
  }
}
