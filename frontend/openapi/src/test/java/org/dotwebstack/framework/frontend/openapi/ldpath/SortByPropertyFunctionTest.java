package org.dotwebstack.framework.frontend.openapi.ldpath;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.dotwebstack.framework.frontend.openapi.ldpath.SortByPropertyFunction.Direction;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class SortByPropertyFunctionTest {

  public static class SortByProperty extends AbstractFunctionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final ImmutableMap<String, String> LD_PATHNAMESPACES =
        ImmutableMap.of("ro", "http://data.informatiehuisruimte.nl/def/ro#");
    private static final String HTTP_EXAMPLE_COM = "http://example.com#";
    private List<String> expectedSorted = new ArrayList<>();

    @Before
    public void setUp() throws IOException {
      Rio.parse(getClass().getResourceAsStream("personsModel.ttl"), "", RDFFormat.TURTLE).forEach(
          this::addStatement);

      Arrays.asList("2", "1", "3").forEach(
          number -> expectedSorted.add(HTTP_EXAMPLE_COM + "Text" + number));
    }

    @Override
    protected LdPathParser<Value> createParserFromString(String program) {
      LdPathParser<Value> parser = super.createParserFromString(program);
      parser.registerFunction(new SortByPropertyFunction<>(LD_PATHNAMESPACES));
      return parser;
    }

    @Test
    public void testSortByVolgnummerForTextChildrenAscending() throws ParseException {
      String ldPath =
          String.format("fn:sortByProperty(ro:kind, \"%s\", \"%s\", \"%s\") :: xsd:string",
              "ro:volgnummer", "number", "asc");

      Collection<Object> result = evaluateRule(ldPath,
          SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));

      assertThat(result.toArray(), equalTo(expectedSorted.toArray()));
    }

    @Test
    public void testSortByTitelForTextChildrenAscending() throws ParseException {
      String ldPath =
          String.format("fn:sortByProperty(ro:kind, \"%s\", \"%s\", \"%s\") :: xsd:string",
              "ro:titel", "string", "asc");

      Collection<Object> result = evaluateRule(ldPath,
          SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));

      assertThat(result.toArray(), equalTo(expectedSorted.toArray()));
    }

    @Test
    public void testSortWithDefaultOrder() throws ParseException {
      String ldPath = "fn:sortByProperty(ro:kind, \"ro:volgnummer\", \"number\" ) :: xsd:string";

      Collection<Object> result = evaluateRule(ldPath,
          SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));

      assertThat(result.toArray(), equalTo(expectedSorted.toArray()));
    }

    @Test
    public void testSortWithWrongFunctionName() throws ParseException {
      expectedException.expect(ParseException.class);

      String ldPath = "fn:sortByProp(ro:kind) :: xsd:string";
      // Throws ParseException
      evaluateRule(ldPath, SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));
    }

    @Test
    public void testSortWithWrongProperty() throws ParseException {
      String ldPath =
          String.format("fn:sortByProperty(ro:kind, \"%s\", \"%s\", \"%s\") :: xsd:string",
              "ro:vlognummer", "string", "asc");

      Collection<Object> result = evaluateRule(ldPath,
          SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));

      assertThat(result, equalTo(Collections.EMPTY_LIST));
    }

    @Test
    public void testSortWithWrongPropertyType() throws ParseException {
      String ldPath =
          String.format("fn:sortByProperty(ro:kind, \"%s\", \"%s\", \"%s\") :: xsd:string",
              "ro:titel", "strig", "asc");

      Collection<Object> result = evaluateRule(ldPath,
          SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));

      assertThat(result.toArray(), equalTo(expectedSorted.toArray()));
    }

    @Test
    public void testSortWithoutPropertyTypeAndDirection() throws ParseException {
      String ldPath = String.format("fn:sortByProperty(ro:kind, \"%s\") :: xsd:string", "ro:titel");

      Collection<Object> result = evaluateRule(ldPath,
          SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));

      assertThat(result.toArray(), equalTo(expectedSorted.toArray()));
    }

    @Test
    public void testSortWithWrongDirection() throws ParseException {
      String ldPath =
          String.format("fn:sortByProperty(ro:kind, \"%s\", \"%s\", \"%s\") :: xsd:string",
              "ro:titel", "string", "ascii");

      Collection<Object> result = evaluateRule(ldPath,
          SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));

      assertThat(result.toArray(), equalTo(expectedSorted.toArray()));
    }

    @Test
    public void testDefaultAllTheThings() throws ParseException {
      expectedException.expect(ArrayIndexOutOfBoundsException.class);
      String ldPath = "fn:sortByProperty() :: xsd:string";

      evaluateRule(ldPath, SimpleValueFactory.getInstance().createIRI("http://example.com#Text"));
    }
  }

  public static class InnerComparator {
    private List<Integer> actual = Arrays.asList(2, 9, 3, 8, 4, 6, 7, 1, 5);

    @Test
    public void compareAscending() throws Exception {
      actual.sort(new SortByPropertyFunction.InnerComparator(Direction.ASC));
      assertThat(actual, equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    public void compareDescending() throws Exception {
      actual.sort(new SortByPropertyFunction.InnerComparator(Direction.DESC));
      assertThat(actual, equalTo(Arrays.asList(9, 8, 7, 6, 5, 4, 3, 2, 1)));
    }
  }
}
