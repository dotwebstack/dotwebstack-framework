package org.dotwebstack.framework.integrationtest.openapirdf4j.matcher;

import java.util.Objects;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsEqualIgnoringLineBreaks extends TypeSafeMatcher<String> {

  private final String string;

  public IsEqualIgnoringLineBreaks(String string) {
    if (Objects.nonNull(string)) {
      this.string = string;
    } else {
      throw new IllegalArgumentException("Given string can't be null.");
    }
  }

  public boolean matchesSafely(String item) {
    String expected = string.replace("\r\n", "")
        .replace("\n", "");
    String actual = item.replace("\r\n", "")
        .replace("\n", "");
    return expected.equals(actual);
  }

  public void describeMismatchSafely(String item, Description mismatchDescription) {
    mismatchDescription.appendText("was ")
        .appendText(item);
  }

  public void describeTo(Description description) {
    description.appendText("equalToIgnoringLineBreaks(")
        .appendValue(this.string)
        .appendText(")");
  }

  public static Matcher<String> equalToIgnoringLineBreaks(String expectedString) {
    return new IsEqualIgnoringLineBreaks(expectedString);
  }
}
