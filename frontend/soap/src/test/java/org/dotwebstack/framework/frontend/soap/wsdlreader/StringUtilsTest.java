package org.dotwebstack.framework.frontend.soap.wsdlreader;

import static org.dotwebstack.framework.frontend.soap.wsdlreader.StringUtils.EMPTY;
import static org.dotwebstack.framework.frontend.soap.wsdlreader.StringUtils.fill;
import static org.dotwebstack.framework.frontend.soap.wsdlreader.StringUtils.join;
import static org.dotwebstack.framework.frontend.soap.wsdlreader.StringUtils.removeLeadingString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StringUtilsTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private String nullString = null;
  private String blankString = " \t\r\n";
  private String string = "/test";
  private String repeatedString = string + string + string;
  private String[] words = new String[]{ "alpha", "beta", "gamma" };

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void isBlank_NullString() {
    // Assert
    assertTrue("null string should be blank", StringUtils.isBlank(nullString));
  }

  @Test
  public void isBlank_BlankString() {
    // Assert
    assertTrue("string with space, tab, newline should be blank", StringUtils.isBlank(blankString));
  }

  @Test
  public void isNotBlank_FilledString() {
    // Assert
    assertFalse(string + " is not blank", StringUtils.isBlank(string));
  }

  @Test
  public void join_All() {
    // Act
    String result = join(words, nullString);

    // Assert
    assertEquals(result, "alphabetagamma");
  }

  @Test
  public void join_Range() {
    // Act
    String result = join(words, ", ", 1, 3);

    // Assert
    assertEquals(result, "beta, gamma");
  }

  @Test
  public void join_Range_StartBeyondEndOfArray() {
    // Assert
    expectedException.expect(ArrayIndexOutOfBoundsException.class);

    // Act
    String result = join(words, ", ", words.length, words.length + 1);
  }

  @Test
  public void removeLeadingString_ReturnsEmpty() {
    // Act
    String result = removeLeadingString(repeatedString, string);

    // Assert
    assertEquals(result, StringUtils.EMPTY);
  }

  @Test
  public void fill_minMax_Repeated() {
    // Act
    String result = fill(words[0], words[1], 18, 19);

    // Assert
    assertEquals(result, "alphabetabetabetabe");
  }

  @Test
  public void fill_minMax_Small() {
    // Act
    String result = fill(words[0], words[1], 3, 4);

    // Assert
    assertEquals(result, "alph");
  }

  @Test
  public void fill_langth() {
    // Act
    String result = fill(words[0], words[1], 18);

    // Assert
    assertEquals(result, "alphabetabetabetab");
  }
}
