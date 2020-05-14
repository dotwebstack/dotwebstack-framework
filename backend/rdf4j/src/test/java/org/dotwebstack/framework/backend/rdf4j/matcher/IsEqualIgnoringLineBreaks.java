package org.dotwebstack.framework.backend.rdf4j.matcher;

import lombok.NonNull;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsEqualIgnoringLineBreaks extends TypeSafeMatcher<String> {

  private final String string;

  public IsEqualIgnoringLineBreaks(@NonNull String string) {
    this.string = string;
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
