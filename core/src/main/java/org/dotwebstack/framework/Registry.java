package org.dotwebstack.framework;

import java.util.HashMap;
import org.dotwebstack.framework.backend.Backend;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;

@Service
public class Registry {

  private HashMap<IRI, Backend> backends = new HashMap<>();

  private HashMap<IRI, InformationProduct> informationProducts = new HashMap<>();

  private HashMap<IRI, Representation> representations = new HashMap<>();

  public void registerBackend(Backend backend) {
    backends.put(backend.getIdentifier(), backend);
  }

  public void registerInformationProduct(InformationProduct product) {
    informationProducts.put(product.getIdentifier(), product);
  }

  public void registerRepresentation(Representation representation) {
    representations.put(representation.getIdentifier(), representation);
  }

  public Backend getBackend(IRI identifier) {
    if (!backends.containsKey(identifier)) {
      throw new IllegalArgumentException(String.format("Backend <%s> not found.", identifier));
    }

    return backends.get(identifier);
  }

  public InformationProduct getInformationProduct(IRI identifier) {
    if (!informationProducts.containsKey(identifier)) {
      throw new IllegalArgumentException(
          String.format("Information product <%s> not found.", identifier));
    }

    return informationProducts.get(identifier);
  }

  public Representation getRepresentation(IRI identifier) {
    if (!representations.containsKey(identifier)) {
      throw new IllegalArgumentException(
          String.format("Representation <%s> not found.", identifier)
      );
    }
    return representations.get(identifier);
  }

  public int getNumberOfBackends() {
    return backends.size();
  }

  public int getNumberOfInformationProducts() {
    return informationProducts.size();
  }

}
