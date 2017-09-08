package org.dotwebstack.framework;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public final class LiteralDataTypes {

  public static final List<IRI> INTEGER_DATA_TYPES;

  public static final List<IRI> DECIMAL_DATA_TYPES;

  public static final List<IRI> BOOLEAN_DATA_TYPES;

  static {
    INTEGER_DATA_TYPES = ImmutableList.of(XMLSchema.INT, XMLSchema.INTEGER, XMLSchema.LONG,
        XMLSchema.NEGATIVE_INTEGER, XMLSchema.NON_NEGATIVE_INTEGER, XMLSchema.NON_POSITIVE_INTEGER,
        XMLSchema.POSITIVE_INTEGER, XMLSchema.SHORT, XMLSchema.UNSIGNED_LONG,
        XMLSchema.UNSIGNED_INT, XMLSchema.UNSIGNED_SHORT, XMLSchema.UNSIGNED_BYTE);

    DECIMAL_DATA_TYPES = ImmutableList.of(XMLSchema.DECIMAL, XMLSchema.FLOAT, XMLSchema.DOUBLE);

    BOOLEAN_DATA_TYPES = ImmutableList.of(XMLSchema.BOOLEAN);
  }

  private LiteralDataTypes() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", LiteralDataTypes.class));
  }

}
