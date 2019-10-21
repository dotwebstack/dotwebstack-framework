package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.sail.memory.model.DecimalMemLiteral;
import org.eclipse.rdf4j.sail.memory.model.IntegerMemLiteral;
import org.eclipse.rdf4j.sail.memory.model.MemLiteral;
import org.junit.jupiter.api.Test;

class CompareHelperTest {

  @Test
  void compareHelper_sortsListAsc_withSimpleStringValue() {
    // Arrange
    List<SimpleLiteral> labels = new ArrayList<>(List.of(new MemLiteral(null, "a"), new MemLiteral(null, "z"),
        new MemLiteral(null, "g"), new MemLiteral(null, "d"), new MemLiteral(null, "b")));

    List<SimpleLiteral> ascending = List.of(new MemLiteral(null, "a"), new MemLiteral(null, "b"),
        new MemLiteral(null, "d"), new MemLiteral(null, "g"), new MemLiteral(null, "z"));

    // Act
    labels.sort(CompareHelper.getComparator(true));

    // Assert
    assertThat(labels, is(equalTo(ascending)));
  }

  @Test
  void compareHelper_sortsListDesc_withSimpleStringValue() {
    // Arrange
    List<SimpleLiteral> labels = new ArrayList<>(List.of(new MemLiteral(null, "a"), new MemLiteral(null, "z"),
        new MemLiteral(null, "g"), new MemLiteral(null, "d"), new MemLiteral(null, "b")));

    List<SimpleLiteral> descending = List.of(new MemLiteral(null, "z"), new MemLiteral(null, "g"),
        new MemLiteral(null, "d"), new MemLiteral(null, "b"), new MemLiteral(null, "a"));

    // Act
    labels.sort(CompareHelper.getComparator(false));

    // Assert
    assertThat(labels, is(equalTo(descending)));
  }

  @Test
  void compareHelper_sortsListAsc_withSimpleIntValue() {
    // Arrange
    IntegerMemLiteral int1 = new IntegerMemLiteral(null, new BigInteger("1"));
    IntegerMemLiteral int5 = new IntegerMemLiteral(null, new BigInteger("5"));
    IntegerMemLiteral int54 = new IntegerMemLiteral(null, new BigInteger("54"));
    IntegerMemLiteral int102 = new IntegerMemLiteral(null, new BigInteger("102"));
    IntegerMemLiteral int1230 = new IntegerMemLiteral(null, new BigInteger("1230"));

    List<SimpleLiteral> labels = new ArrayList<>(List.of(int54, int1, int1230, int5, int102));
    List<SimpleLiteral> ascending = List.of(int1, int5, int54, int102, int1230);

    // Act
    labels.sort(CompareHelper.getComparator(true));

    // Assert
    assertThat(labels, is(equalTo(ascending)));
  }

  @Test
  void compareHelper_sortsListDesc_withSimpleIntValue() {
    // Arrange
    IntegerMemLiteral int1 = new IntegerMemLiteral(null, new BigInteger("1"));
    IntegerMemLiteral int5 = new IntegerMemLiteral(null, new BigInteger("5"));
    IntegerMemLiteral int54 = new IntegerMemLiteral(null, new BigInteger("54"));
    IntegerMemLiteral int102 = new IntegerMemLiteral(null, new BigInteger("102"));
    IntegerMemLiteral int1230 = new IntegerMemLiteral(null, new BigInteger("1230"));

    List<SimpleLiteral> labels = new ArrayList<>(List.of(int54, int1, int1230, int5, int102));
    List<SimpleLiteral> descending = List.of(int1230, int102, int54, int5, int1);

    // Act
    labels.sort(CompareHelper.getComparator(false));

    // Assert
    assertThat(labels, is(equalTo(descending)));
  }

  @Test
  void compareHelper_sortsListAsc_withSimpleDecimalValue() {
    // Arrange
    DecimalMemLiteral int1 = new DecimalMemLiteral(null, new BigDecimal("1.02"));
    DecimalMemLiteral int503 = new DecimalMemLiteral(null, new BigDecimal("5.03"));
    DecimalMemLiteral int5 = new DecimalMemLiteral(null, new BigDecimal("5.4"));
    DecimalMemLiteral int54 = new DecimalMemLiteral(null, new BigDecimal("54.102"));
    DecimalMemLiteral int123 = new DecimalMemLiteral(null, new BigDecimal("123.0"));
    DecimalMemLiteral int1230 = new DecimalMemLiteral(null, new BigDecimal("1230."));

    List<SimpleLiteral> ascending = List.of(int1, int503, int5, int54, int123, int1230);
    List<SimpleLiteral> labels = new ArrayList<>(List.of(int54, int1, int1230, int5, int123, int503));

    // Act
    labels.sort(CompareHelper.getComparator(true));

    // Assert
    assertThat(labels, is(equalTo(ascending)));
  }

  @Test
  void compareHelper_sortsListDesc_withSimpleDecimalValue() {
    // Arrange
    DecimalMemLiteral int1 = new DecimalMemLiteral(null, new BigDecimal("1.02"));
    DecimalMemLiteral int503 = new DecimalMemLiteral(null, new BigDecimal("5.03"));
    DecimalMemLiteral int5 = new DecimalMemLiteral(null, new BigDecimal("5.4"));
    DecimalMemLiteral int54 = new DecimalMemLiteral(null, new BigDecimal("54.102"));
    DecimalMemLiteral int123 = new DecimalMemLiteral(null, new BigDecimal("123.0"));
    DecimalMemLiteral int1230 = new DecimalMemLiteral(null, new BigDecimal("1230."));

    List<SimpleLiteral> descending = List.of(int1230, int123, int54, int5, int503, int1);
    List<SimpleLiteral> labels = new ArrayList<>(List.of(int54, int1, int1230, int5, int123, int503));

    // Act
    labels.sort(CompareHelper.getComparator(false));

    // Assert
    assertThat(labels, is(equalTo(descending)));
  }
}
