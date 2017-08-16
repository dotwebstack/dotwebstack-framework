package org.dotwebstack.framework;

import org.eclipse.rdf4j.model.IRI;
import java.util.Objects;

public class Stage {
    private static final String DEFAULT_BASE_PATH = "/";

    private IRI identifier;

    private Site site;

    private String basePath;

    private Stage(Builder builder) {
        this.identifier = builder.identifier;
        this.site = builder.site;
        this.basePath = builder.basePath;
    }

    public IRI getIdentifier() {
        return identifier;
    }

    public Site getSite() {
        return site;
    }

    public String getBasePath() {
        return basePath;
    }

    public static class Builder {
        private IRI identifier;
        private Site site;

        private String basePath = DEFAULT_BASE_PATH;

        public Builder(IRI identifier, Site site) {
            this.identifier = Objects.requireNonNull(identifier);
            this.site = Objects.requireNonNull(site);
        }

        public Builder basePath(String basePath) {
            this.basePath = Objects.requireNonNull(basePath);
            return this;
        }

        public Stage build() { return new Stage(this);}
    }
}
