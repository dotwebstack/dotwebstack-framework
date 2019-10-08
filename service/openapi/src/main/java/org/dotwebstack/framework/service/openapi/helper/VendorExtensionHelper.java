package org.dotwebstack.framework.service.openapi.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

public class VendorExtensionHelper {

  private static final String VENDOR_EXTENSION_DOTWEBSTACK = "x-dws";

  private VendorExtensionHelper() {}

  public static ObjectNode removeVendorExtensions(@NonNull InputStream specStream, @NonNull YAMLMapper mapper)
      throws IOException {
    ObjectNode specNode = mapper.readValue(specStream, ObjectNode.class);
    removeExtensionNodes(specNode);
    return specNode;
  }


  private static void removeExtensionNodes(ContainerNode<?> node) {
    if (node.isObject()) {
      removeExtensionNodesFromObject((ObjectNode) node);
    } else if (node.isArray()) {
      ArrayNode arrayNode = (ArrayNode) node;
      arrayNode.forEach(arrayElement -> {
        if (arrayElement.isContainerNode()) {
          removeExtensionNodes((ContainerNode<?>) arrayElement);
        }
      });
    }
    // other elements are 'textnodes' and do not need to be removed
  }

  private static void removeExtensionNodesFromObject(ObjectNode node) {
    List<String> passedFields = new ArrayList<>();
    node.fieldNames()
        .forEachRemaining(name -> {
          if (!name.startsWith(VENDOR_EXTENSION_DOTWEBSTACK)) {
            passedFields.add(name);
          }
        });
    node.retain(passedFields);
    node.fieldNames()
        .forEachRemaining(name -> {
          JsonNode jsonNode = node.get(name);
          if (jsonNode.isContainerNode()) {
            removeExtensionNodes((ContainerNode<?>) jsonNode);
          }
        });
  }

}
