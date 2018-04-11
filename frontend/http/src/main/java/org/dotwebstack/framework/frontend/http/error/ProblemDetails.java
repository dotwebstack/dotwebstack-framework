package org.dotwebstack.framework.frontend.http.error;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;

/**
 * Object representing a "Problem Details for HTTP APIs", conform RFC7807
 *
 * <p>
 * See <a href="https://tools.ietf.org/html/rfc7807#section-3.1">
 * https://tools.ietf.org/html/rfc7807#section-3.1</a>
 * </p>
 */
@JsonInclude(Include.NON_NULL)
public class ProblemDetails {

  private String title;
  private int status;
  private String detail;

  /**
   * Extended properties See <a href="https://tools.ietf.org/html/rfc7807#section-3.2">
   * https://tools.ietf.org/html/rfc7807#section-3.2</a>
   */
  private Map<String, Object> extendedDetails = new HashMap<>();

  public String getTitle() {
    return title;
  }

  void setTitle(String title) {
    this.title = title;
  }

  public int getStatus() {
    return status;
  }

  void setStatus(int status) {
    this.status = status;
  }

  public String getDetail() {
    return detail;
  }

  void setDetail(String detail) {
    this.detail = detail;
  }

  public void setExtendedDetails(Map<String, Object> extendedDetails) {
    this.extendedDetails = extendedDetails;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtendedDetails() {
    return extendedDetails;
  }
}
