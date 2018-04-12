package org.dotwebstack.framework.frontend.openapi.handlers;

import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.transaction.Transaction;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class TransactionBodyMapper {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionBodyMapper.class);

  Model map(@NonNull Operation operation, @NonNull Transaction transaction,
      @NonNull RequestParameters requestParameters) {
    for (io.swagger.models.parameters.Parameter openApiParameter : operation.getParameters()) {

      if (openApiParameter instanceof BodyParameter) {
        Map<String, Object> vendorExtensions = openApiParameter.getVendorExtensions();

        LOG.debug("Vendor extensions for body parameter '{}': {}", openApiParameter.getName(),
            vendorExtensions);

        Object rmlMapping = vendorExtensions.get(OpenApiSpecificationExtensions.RML_MAPPING);

        if (rmlMapping == null) {
          throw new RequestHandlerRuntimeException(
              "Vendor extension x-dotwebstack-rml-mapping not found for body parameter");
        }

        String value = requestParameters.getRawBody();

        InputStream mappingAsInputStream = IOUtils.toInputStream(
            "@prefix rr: <http://www.w3.org/ns/r2rml#>.\n"
            + "@prefix rml: <http://semweb.mmlab.be/ns/rml#>.\n"
            + "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n"
            + "@prefix carml: <http://carml.taxonic.com/carml/> .\n"
            + "@prefix ex: <http://www.example.com/> .\n"
            + "\n"
            + "<#SubjectMapping> a rr:TriplesMap ;\n"
            + "  rml:logicalSource [\n"
            + "    rml:source [\n"
            + "      a carml:Stream ;\n"
            + "      carml:streamName \"stream-A\"\n"
            + "    ] ;\n"
            + "    rml:referenceFormulation ql:JSONPath ;\n"
            + "    rml:iterator \"$\" ;\n"
            + "  ] ;\n"
            + "\n"
            + "  rr:subjectMap [\n"
            + "    rr:template \"http://www.example.com/{name}\" ;\n"
            + "  ] ;\n"
            + "\n"
            + "  rr:predicateObjectMap [\n"
            + "    rr:predicate ex:ownsCar ;\n"
            + "    rr:objectMap [\n"
            + "      carml:multiReference \"cars\" ;\n"
            + "      rr:datatype xsd:string ;\n"
            + "    ] ;\n"
            + "  ] ;\n"
            + ".\n");
        // todo get mapping from resource loader

        Set<TriplesMap> mapping = RmlMappingLoader.build().load(mappingAsInputStream,
            RDFFormat.TURTLE);

        RmlMapper mapper = RmlMapper.newBuilder().build();

        InputStream inputStream = IOUtils.toInputStream(value);
        mapper.bindInputStream("stream-A", inputStream);

        Model transactionModel = mapper.map(mapping);

        return transactionModel;
      }
    }

    throw new RequestHandlerRuntimeException("body parameter not found");
  }

}

