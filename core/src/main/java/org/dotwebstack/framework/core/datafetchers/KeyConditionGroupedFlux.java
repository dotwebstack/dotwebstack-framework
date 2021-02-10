package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;

public class KeyConditionGroupedFlux extends GroupedFlux<KeyCondition, Map<String, Object>> {
  private final KeyCondition keyCondition;

  private final Flux<Map<String, Object>> flux;

  public KeyConditionGroupedFlux(KeyCondition keyCondition, Flux<Map<String, Object>> flux) {
    this.keyCondition = keyCondition;
    this.flux = flux;
  }

  @Override
  public KeyCondition key() {
    return keyCondition;
  }

  @Override
  public void subscribe(CoreSubscriber<? super Map<String, Object>> coreSubscriber) {
    flux.subscribe(coreSubscriber);
  }
}
