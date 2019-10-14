package org.dotwebstack.framework.backend.rdf4j.helper;

import java.util.Comparator;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.DecimalLiteral;
import org.eclipse.rdf4j.model.impl.IntegerLiteral;
import org.eclipse.rdf4j.model.impl.NumericLiteral;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.sail.memory.model.DecimalMemLiteral;
import org.eclipse.rdf4j.sail.memory.model.IntegerMemLiteral;
import org.eclipse.rdf4j.sail.memory.model.NumericMemLiteral;

public class CompareHelper {

  private CompareHelper() {}

  public static Comparator<Value> getComparator(boolean asc) {
    return new Comparator<Value>() {
      @Override
      public int compare(Value value1, Value value2) {
        if (value1 instanceof IntegerLiteral || value1 instanceof IntegerMemLiteral || value1 instanceof NumericLiteral
            || value1 instanceof NumericMemLiteral) {
          return compareIntegerLiteral((SimpleLiteral) value1, (SimpleLiteral) value2, asc);
        }

        if (value1 instanceof DecimalLiteral || value1 instanceof DecimalMemLiteral) {
          return compareDecimalLiteral((SimpleLiteral) value1, (SimpleLiteral) value2, asc);
        }

        return compareValue(value1, value2, asc);
      }
    };
  }

  private static int compareValue(Value value1, Value value2, boolean asc) {
    if (asc) {
      return value1.stringValue()
          .compareTo(value2.stringValue());
    }
    return value2.stringValue()
        .compareTo(value1.stringValue());
  }

  private static int compareIntegerLiteral(SimpleLiteral integerLiteral1, SimpleLiteral integerLiteral2, boolean asc) {
    if (asc) {
      return integerLiteral1.integerValue()
          .compareTo(integerLiteral2.integerValue());
    }
    return integerLiteral2.integerValue()
        .compareTo(integerLiteral1.integerValue());
  }

  private static int compareDecimalLiteral(SimpleLiteral integerLiteral1, SimpleLiteral integerLiteral2, boolean asc) {
    if (asc) {
      return integerLiteral1.floatValue() > integerLiteral2.floatValue() ? 1 : -1;
    }
    return integerLiteral2.floatValue() > integerLiteral1.floatValue() ? 1 : -1;
  }

}
