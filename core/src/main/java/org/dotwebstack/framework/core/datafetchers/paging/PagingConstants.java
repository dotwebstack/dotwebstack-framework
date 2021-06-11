package org.dotwebstack.framework.core.datafetchers.paging;

import java.math.BigInteger;

public final class PagingConstants {

  private PagingConstants() {}

  public static final int FIRST_MAX_VALUE = 100;

  public static final int OFFSET_MAX_VALUE = 10000;

  public static final String TYPE_SUFFIX_NAME = "Connection";

  public static final String NODES_FIELD_NAME = "nodes";

  public static final String OFFSET_FIELD_NAME = "offset";

  public static final String FIRST_ARGUMENT_NAME = "first";

  public static final BigInteger FIRST_DEFAULT_VALUE = BigInteger.ZERO;

  public static final String OFFSET_ARGUMENT_NAME = "offset";

  public static final BigInteger OFFSET_DEFAULT_VALUE = BigInteger.valueOf(10);
}
