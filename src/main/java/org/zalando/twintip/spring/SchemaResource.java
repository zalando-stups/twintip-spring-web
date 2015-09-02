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
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

@RestController
public class SchemaResource {

    public static final String SCHEMA_DISCOVERY_MAPPING = "/.well-known/schema-discovery";

    private final Yaml yaml = new Yaml();

    private final ObjectMapper objectMapper;

    private final String apiYamlValue;

    private final String apiJsonValue;

    @Autowired
    public SchemaResource(@Value("${twintip.yaml}") final Resource yamlResource) throws IOException {
        this.objectMapper = new ObjectMapper();
        apiYamlValue = consume(yamlResource.getInputStream());
        apiJsonValue = yamlToJson(apiYamlValue);
    }

    @RequestMapping(method = RequestMethod.GET, value = SCHEMA_DISCOVERY_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public SchemaDiscovery discover(
            @Value("${twintip.mapping}") final String mapping,
            @Value("${twintip.type:swagger-2.0}") final String type,
            @Value("${twintip.ui:}") final String uiPath) {
        return new SchemaDiscovery(mapping, type, uiPath.isEmpty() ? Optional.empty() : Optional.of(uiPath));
    }

    @RequestMapping(method = RequestMethod.GET, value = "${twintip.mapping}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> schemaAsJson() {
        return response(apiJsonValue);
    }

    @RequestMapping(method = RequestMethod.GET, value = "${twintip.mapping}", produces = {"application/yaml", "application/x-yaml", "text/yaml"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> schemaAsYaml() {
        return response(apiYamlValue);
    }

    private ResponseEntity<String> response(final String body) {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .header("Access-Control-Max-Age", "3600")
                .header("Access-Control-Allow-Headers", "")
                .body(body);
    }

    @SuppressWarnings("unchecked")
    private String yamlToJson(final String yamlValue) throws JsonProcessingException {
        return objectMapper.writeValueAsString(yaml.load(yamlValue));
    }

    public static String consume(final InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(joining(lineSeparator()));
        }
    }

}
