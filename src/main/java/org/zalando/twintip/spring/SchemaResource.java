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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

@RestController
public class SchemaResource {

    public static final String SCHEMA_DISCOVERY_MAPPING = "/.well-known/schema-discovery";

    private final ObjectMapper json;
    private final ObjectMapper yaml;

    private final Map<String, Object> node;
    private final boolean enableCors;

    @Autowired
    @SuppressWarnings("unchecked")
    public SchemaResource(
        @Value("${twintip.yaml}") final Resource yamlResource,
        @Value("${twintip.cors:true}") final boolean enableCors,
        @Value("${twintip.baseUrl:}") final String baseUrl) throws IOException {

        this.json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.yaml = new ObjectMapper(new YAMLFactory());

        this.node = yaml.readValue(yamlResource.getInputStream(), Map.class);
        this.enableCors = enableCors;

        if (!baseUrl.isEmpty()) {
            updateApiUrl(node, URI.create(baseUrl));
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = SCHEMA_DISCOVERY_MAPPING,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public SchemaDiscovery discover(
        @Value("${twintip.mapping}") final String mapping,
        @Value("${twintip.type:swagger-2.0}") final String type,
        @Value("${twintip.ui:}") final String uiPath) {
        return new SchemaDiscovery(mapping, type, uiPath);
    }

    @RequestMapping(method = RequestMethod.GET, value = "${twintip.mapping}",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> jsonSchema() throws JsonProcessingException {
        return response(json.writeValueAsString(node));
    }

    @RequestMapping(method = RequestMethod.GET, value = "${twintip.mapping}",
        produces = {"application/yaml", "application/x-yaml", "text/yaml"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> yamlSchema() throws JsonProcessingException {
        return response(yaml.writeValueAsString(node));
    }

    private ResponseEntity<String> response(final String body) {
        final ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (enableCors) {
            builder
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .header("Access-Control-Max-Age", "3600")
                .header("Access-Control-Allow-Headers", "");
        }
        return builder.body(body);
    }

    private void updateApiUrl(final Map<String, Object> apiDef, final URI baseUrl) {
        apiDef.put("host", Objects.requireNonNull(baseUrl.getHost(), "baseUrl must contain host") + (baseUrl.getPort() != -1 ? ":" + baseUrl.getPort() : ""));
        apiDef.put("schemes", new String[]{Objects.requireNonNull(baseUrl.getScheme(), "baseUrl must contain scheme")});
        apiDef.put("basePath", Objects.requireNonNull(baseUrl.getPath(), "baseUrl must contain path"));
    }
}
