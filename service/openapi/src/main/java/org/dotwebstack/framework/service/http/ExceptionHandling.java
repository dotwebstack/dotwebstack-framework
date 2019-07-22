package org.dotwebstack.framework.service.http;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.webflux.advice.general.GeneralAdviceTrait;
import org.zalando.problem.spring.webflux.advice.http.HttpAdviceTrait;

@ControllerAdvice
class ExceptionHandling implements GeneralAdviceTrait, HttpAdviceTrait {

}
