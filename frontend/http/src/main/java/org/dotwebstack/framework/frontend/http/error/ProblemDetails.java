package org.dotwebstack.framework.frontend.http.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.dotwebstack.framework.frontend.http.error.InvalidParamsBadRequestException.InvalidParameter;

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
   * https://tools.ietf.org/html/rfc7807#section-3.2</a> currently only used for invalid params
   */
  @JsonProperty("invalidParams")
  @JsonInclude(Include.NON_EMPTY)
  private List<InvalidParameter> extendedDetails = new ArrayList<>();

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

  public void setInvalidParams(List<InvalidParameter> list) {
    this.extendedDetails = list;
  }

  // @JsonAnyGetter
  public List<InvalidParameter> getExtendedDetails() {
    return extendedDetails;
  }
}
