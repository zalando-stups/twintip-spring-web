package org.zalando.twintip.spring;

/*
 * #%L
 * twintip-spring-web
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class SchemaDiscovery {

    @JsonProperty("schema_url")
    private final String schemaUrl;

    @JsonProperty("schema_type")
    private final String schemaType;

    @JsonProperty("ui_url")
    @JsonInclude(NON_EMPTY)
    private final Optional<String> uiUrl;

    public SchemaDiscovery(final String schemaUrl, final String schemaType, final Optional<String> uiUrl) {
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

    public Optional<String> getUiUrl() {
        return uiUrl;
    }
}
