package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.sail.memory.model.DecimalMemLiteral;
import org.eclipse.rdf4j.sail.memory.model.IntegerMemLiteral;
import org.eclipse.rdf4j.sail.memory.model.MemLiteral;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class CompareHelperTest {
  @TestFactory
  Stream<DynamicContainer> compare() {
    return Stream.of(getTestCases("String", stringListProvider(), "a", "b", "d", "g", "z"),
        getTestCases("BigInteger", intListProvider(), "1", "5", "54", "102", "1230"),
        getTestCases("BigDecimal", decimalListProvider(), "1.02", "5.4", "54.102", "123.0", "1230."));
  }

  private DynamicContainer getTestCases(String type, Function<List<String>, ArrayList<SimpleLiteral>> provider,
      String first, String second, String third, String fourth, String fifth) {
    List<String> unsorted = List.of(second, fifth, fourth, third, first);

    return dynamicContainer("With " + type + " values",
        Stream.of(getTestCase(provider, unsorted, true, List.of(first, second, third, fourth, fifth)),
            getTestCase(provider, unsorted, false, List.of(fifth, fourth, third, second, first))));
  }

  private DynamicTest getTestCase(Function<List<String>, ArrayList<SimpleLiteral>> provider, List<String> labels,
      boolean ascending, List<String> sorted) {
    return dynamicTest("Sorts list " + (ascending ? "ascending" : "descending"), () -> {
      // Arrange
      List<SimpleLiteral> unsorted = provider.apply(labels);
      // Act
      unsorted.sort(CompareHelper.getComparator(ascending));
      // Assert
      assertThat(unsorted, is(equalTo(provider.apply(sorted))));
    });
  }

  private Function<List<String>, ArrayList<SimpleLiteral>> stringListProvider() {
    return labels -> labels.stream()
        .map(label -> new MemLiteral(null, label))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private Function<List<String>, ArrayList<SimpleLiteral>> intListProvider() {
    return labels -> labels.stream()
        .map(BigInteger::new)
        .map(label -> new IntegerMemLiteral(null, label))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private Function<List<String>, ArrayList<SimpleLiteral>> decimalListProvider() {
    return labels -> labels.stream()
        .map(BigDecimal::new)
        .map(label -> new DecimalMemLiteral(null, label))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
