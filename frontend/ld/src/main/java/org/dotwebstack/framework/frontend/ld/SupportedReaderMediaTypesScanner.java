package org.dotwebstack.framework.frontend.ld;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportedReaderMediaTypesScanner {

  private static final Logger LOG = LoggerFactory.getLogger(SupportedReaderMediaTypesScanner.class);

  private final List<MessageBodyReader<Model>> modelReaders = new ArrayList<>();

  private final List<MediaType> mediaTypes = new ArrayList<>();

  @Autowired
  public SupportedReaderMediaTypesScanner(List<MessageBodyReader<Model>> modelReaders) {
    loadSupportedMediaTypes(modelReaders);
  }

  private void loadSupportedMediaTypes(List<MessageBodyReader<Model>> modelReaders) {
    modelReaders.forEach(reader -> {
      Consumes consumesAnnotation = reader.getClass().getAnnotation(Consumes.class);
      if (consumesAnnotation == null) {
        LOG.warn(String.format(
            "Found reader that did not specify the Consume annotation correctly. Skipping %s",
            reader.getClass()));
        return;
      }

      this.modelReaders.add(reader);
      for (String mediaType : consumesAnnotation.value()) {
        mediaTypes.add(MediaType.valueOf(mediaType));
      }
    });
  }

  public List<MessageBodyReader<Model>> getModelReaders() {
    return ImmutableList.copyOf(modelReaders);
  }

  public List<MediaType> getMediaTypes() {
    return mediaTypes;
  }

}
