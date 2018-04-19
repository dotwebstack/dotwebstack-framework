package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.ImmutableSet;
import com.taxonic.carml.model.FileSource;
import com.taxonic.carml.model.NameableStream;
import com.taxonic.carml.model.TermType;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.model.XmlSource;
import com.taxonic.carml.model.impl.CarmlFileSource;
import com.taxonic.carml.model.impl.CarmlStream;
import com.taxonic.carml.model.impl.CarmlTriplesMap;
import com.taxonic.carml.model.impl.CarmlXmlSource;
import com.taxonic.carml.rdf_mapper.impl.MappingCache;
import com.taxonic.carml.rdf_mapper.util.RdfObjectLoader;
import com.taxonic.carml.util.RmlConstantShorthandExpander;
import com.taxonic.carml.vocab.Rdf;
import com.taxonic.carml.vocab.Rdf.Rr;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

/*
temp: can be removed when added to Carml
 */
public class RmlCustomMappingLoader {

  private RmlConstantShorthandExpander shorthandExpander;

  public RmlCustomMappingLoader(
      RmlConstantShorthandExpander shorthandExpander
  ) {
    this.shorthandExpander = shorthandExpander;
  }

  public Set<TriplesMap> load(Model originalModel) {
    return
        ImmutableSet.<TriplesMap>copyOf(
            RdfObjectLoader.load(
                selectTriplesMaps,
                CarmlTriplesMap.class,
                originalModel,
                shorthandExpander,
                this::addTermTypes,
                m -> {
                  m.addDecidableType(Rdf.Carml.Stream, NameableStream.class);
                  m.addDecidableType(Rdf.Carml.XmlDocument, XmlSource.class);
                  m.addDecidableType(Rdf.Carml.FileSource, FileSource.class);
                  m.bindInterfaceImplementation(NameableStream.class, CarmlStream.class);
                  m.bindInterfaceImplementation(XmlSource.class, CarmlXmlSource.class);
                  m.bindInterfaceImplementation(FileSource.class, CarmlFileSource.class);
                }
            )
        );
  }

  private void addTermTypes(MappingCache cache) {
    class AddTermTypes {

      void add(IRI iri, TermType termType) {
        cache.addCachedMapping(iri, ImmutableSet.of(TermType.class), termType);
      }

      void run() {
        add(Rr.BlankNode, TermType.BLANK_NODE);
        add(Rr.IRI, TermType.IRI);
        add(Rr.Literal, TermType.LITERAL);
      }
    }

    new AddTermTypes().run();
  }

  public static RmlCustomMappingLoader build() {
    return new RmlCustomMappingLoader(
        new RmlConstantShorthandExpander()
    );
  }

  private static Function<Model, Set<Resource>> selectTriplesMaps =
      model ->
          ImmutableSet.copyOf(
              model
                  .filter(null, Rdf.Rml.logicalSource, null)
                  .subjects()
          );
}
