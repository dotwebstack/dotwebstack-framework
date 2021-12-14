package org.dotwebstack.framework.service.openapi.response.header;

import java.util.function.Consumer;
import org.springframework.http.HttpHeaders;

public interface ResponseHeaderResolver extends Consumer<HttpHeaders> {
}
