package org.zalando.twintip.spring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class SchemaDiscovery {

    @JsonProperty("schema_url")
    private final String schemaUrl;

    @JsonProperty("schema_type")
    private final String schemaType;

    @JsonProperty("ui_url")
    @JsonInclude(NON_EMPTY)
    private final String uiUrl;

    public SchemaDiscovery(final String schemaUrl, final String schemaType, final String uiUrl) {
        this.schemaUrl = schemaUrl;
        this.schemaType = schemaType;
        this.uiUrl = uiUrl;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public String getSchemaType() {
        return schemaType;
    }

    public String getUiUrl() {
        return uiUrl;
    }
}
