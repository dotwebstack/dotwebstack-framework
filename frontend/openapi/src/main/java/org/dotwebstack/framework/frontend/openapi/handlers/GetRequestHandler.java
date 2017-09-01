package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Objects;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  InformationProduct informationProduct;

  public GetRequestHandler(InformationProduct informationProduct) {
    this.informationProduct = Objects.requireNonNull(informationProduct);
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();
    LOG.debug("Handling GET request for path {}", path);

    //TODO: Query the actual information product
    Model builder = new ModelBuilder().subject("http://dbeerpedia.org#Heineken")
        .add(RDF.TYPE, SimpleValueFactory.getInstance().createIRI("http://dbeerpedia.org#Breweries"))
        .build();

    return Response.ok(builder).build();
  }

}
