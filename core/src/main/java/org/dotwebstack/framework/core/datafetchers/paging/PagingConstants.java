package org.dotwebstack.framework.core.datafetchers.paging;

import java.math.BigInteger;

public final class PagingConstants {

  private PagingConstants() {}

  public static final String NODES_FIELD_NAME = "nodes";

  public static final String OFFSET_FIELD_NAME = "offset";

  public static final String FIRST_ARGUMENT_NAME = "first";

  public static final BigInteger FIRST_DEFAULT_VALUE = BigInteger.valueOf(10);

  public static final String OFFSET_ARGUMENT_NAME = "offset";

  public static final BigInteger OFFSET_DEFAULT_VALUE = BigInteger.ZERO;
}
