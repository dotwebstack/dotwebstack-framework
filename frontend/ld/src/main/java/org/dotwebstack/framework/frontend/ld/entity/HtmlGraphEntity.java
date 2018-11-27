package org.dotwebstack.framework.frontend.ld.entity;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.result.HtmlGraphResult;

public class HtmlGraphEntity extends AbstractEntity<HtmlGraphResult> {

  public HtmlGraphEntity(@NonNull HtmlGraphResult result, @NonNull Representation representation) {
    super(result, representation);
  }

}
