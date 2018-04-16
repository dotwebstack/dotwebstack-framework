package org.dotwebstack.framework.queryvisitor;

import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

public class FederatedQueryVisitor extends AbstractQueryModelVisitor {

  @Override
  public void meet(Service service) {
    // first impl. just show value content
    System.out.println("***");
    System.out.println("Service endpoint [name] " + service.getServiceRef().getName());
    System.out.println("Service endpoint [value] " + service.getServiceRef().getValue());
    System.out.println("***");
  }

}
