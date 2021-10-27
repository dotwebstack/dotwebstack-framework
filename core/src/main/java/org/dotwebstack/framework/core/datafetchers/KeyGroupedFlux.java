package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;

public class KeyGroupedFlux extends GroupedFlux<Map<String, Object>, Map<String, Object>> {
  private final Map<String, Object> key;

  private final Flux<Map<String, Object>> flux;

  public KeyGroupedFlux(Map<String, Object> key, Flux<Map<String, Object>> flux) {
    this.key = key;
    this.flux = flux;
  }

  @Override
  public Map<String, Object> key() {
    return key;
  }

  @Override
  public void subscribe(CoreSubscriber<? super Map<String, Object>> coreSubscriber) {
    flux.subscribe(coreSubscriber);
  }
}
