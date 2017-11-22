package org.dotwebstack.framework.frontend.http.error;

import lombok.NonNull;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.ErrorPageRegistrar;
import org.springframework.boot.web.servlet.ErrorPageRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ServletErrorPageRegistrar implements ErrorPageRegistrar {

  @Override
  public void registerErrorPages(@NonNull ErrorPageRegistry registry) {
    for (HttpStatus httpStatus : HttpStatus.values()) {
      registry.addErrorPages(new ErrorPage(httpStatus,
          String.format("/%s/%d", ErrorModule.SERVLET_ERROR_PATH_PREFIX, httpStatus.value())));
    }
  }

}
