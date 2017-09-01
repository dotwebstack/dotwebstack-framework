package org.dotwebstack.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EnvVariableParser {

  private static final Logger LOG = LoggerFactory.getLogger(EnvVariableParser.class);

  private Map<String, String> envVars;

  public EnvVariableParser() {
    envVars = System.getenv();
  }

  public String parse(String input) {

    String patternWithBrackets = "\\$\\{([A-Za-z0-9]+)\\}";
    String patternWithoutBrackets = "\\$([A-Za-z0-9]+)";

    String output = parsePattern(input, patternWithBrackets);

    return parsePattern(output, patternWithoutBrackets);
  }

  private String parsePattern(String text, String pattern) {
    // construct and compile reg expression
    Pattern expr = Pattern.compile(pattern);
    Matcher matcher = expr.matcher(text);

    // find pattern
    while (matcher.find()) {
      String foundEnvVariables = matcher.group(1);
      String envValue = envVars.get(foundEnvVariables);
      if (envValue == null) {
        LOG.error(String.format("Env variable {} found but not defined", foundEnvVariables));
        continue;
      }

      // replace the env variable
      envValue = envValue.replace("\\", "\\\\");
      Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
      text = subexpr.matcher(text).replaceAll(envValue);
    }

    return text;
  }

  public InputStream parse(InputStream input) throws IOException {

    // create buffered reader
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
    BufferedWriter bufferedWriter = null;
    ByteArrayOutputStream byteArrayOutputStream = null;

    try {
      // create buffered writer
      byteArrayOutputStream = new ByteArrayOutputStream();
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
      bufferedWriter = new BufferedWriter(outputStreamWriter);

      // read lines and parse
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        bufferedWriter.write(parse(line));
      }
    } finally {
      if (bufferedWriter != null) {
        bufferedWriter.close();
      }
    }

    // convert outputstream to inputstream
    byte[] byteArray = byteArrayOutputStream.toByteArray();
    InputStream updatedInputStream = new ByteArrayInputStream(byteArray);

    return updatedInputStream;

  }
}
