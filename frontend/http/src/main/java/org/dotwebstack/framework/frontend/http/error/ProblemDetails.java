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

  private ProblemDetails(String title, int status, String detail,
      List<InvalidParameter> extendedDetails) {
    super();
    this.title = title;
    this.status = status;
    this.detail = detail;
    this.invalidParameters = extendedDetails;
  }

  private String title;
  private int status;
  private String detail;

  /**
   * Extended properties See <a href="https://tools.ietf.org/html/rfc7807#section-3.2">
   * https://tools.ietf.org/html/rfc7807#section-3.2</a> currently only used for invalid params
   */
  @JsonProperty("invalidParams")
  @JsonInclude(Include.NON_EMPTY)
  private List<InvalidParameter> invalidParameters = new ArrayList<>();

  public String getTitle() {
    return title;
  }

  public int getStatus() {
    return status;
  }

  public String getDetail() {
    return detail;
  }

  // @JsonAnyGetter
  public List<InvalidParameter> getInvalidParameters() {
    return invalidParameters;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String title;
    private int status;
    private String detail;
    private List<InvalidParameter> invalidParameters = new ArrayList<>();

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withDetail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder withStatus(int status) {
      this.status = status;
      return this;
    }

    public Builder withInvalidParameters(List<InvalidParameter> params) {
      this.invalidParameters = params;
      return this;
    }

    public ProblemDetails build() {
      return new ProblemDetails(title, status, detail, invalidParameters);
    }
  }
}
