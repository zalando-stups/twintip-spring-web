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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.hamcrest.FeatureMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
@TestPropertySource(properties = {
    "twintip.mapping=" + SchemaResourceTest.API_PATH,
    "twintip.yaml=classpath:/petstore.yml",
    "twintip.baseUrl=http://petstore.swagger.io/v1"
})
public class SchemaResourceTest {

    static final String API_PATH = "/api";

    @Value("${twintip.yaml}")
    private Resource yamlResource;

    @Autowired
    private MockMvc mvc;

    @Test
    public void schemaDiscoveryWithDefaults() throws Exception {
        mvc.perform(request(HttpMethod.GET, SchemaResource.SCHEMA_DISCOVERY_MAPPING))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.schema_url", is(API_PATH)))
            .andExpect(jsonPath("$.schema_type", is("swagger-2.0")))
            .andExpect(jsonPath("$", not(hasKey("ui_url"))));
    }

    @Test
    public void apiWithoutAcceptAsJson() throws Exception {
        mvc.perform(request(HttpMethod.GET, API_PATH))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.host", is("petstore.swagger.io")))
            .andExpect(jsonPath("$.schemes", hasSize(1)));
    }

    @Test
    public void apiWithAcceptAsJson() throws Exception {
        mvc.perform(request(HttpMethod.GET, API_PATH)
            .accept(APPLICATION_JSON))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.host", is("petstore.swagger.io")));
    }

    @Test
    public void apiWithAcceptAsYaml() throws Exception {
        final MediaType yaml = MediaType.parseMediaType("application/yaml");
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode expected = mapper.readTree(yamlResource.getInputStream());

        mvc.perform(request(HttpMethod.GET, API_PATH)
            .accept(yaml))
            .andExpect(content().contentType(yaml))
            .andExpect(content().string(new FeatureMatcher<String, JsonNode>(equalTo(expected), "yaml", "yaml") {
                @Override
                protected JsonNode featureValueOf(String actual) {
                    try {
                        return mapper.readTree(actual);
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                }
            }));
    }

    @Test
    public void apiWithCorsEnabled() throws Exception {
        mvc.perform(request(HttpMethod.GET, API_PATH))
            .andExpect(header().string("Access-Control-Allow-Origin", "*"))
            .andExpect(header().string("Access-Control-Allow-Methods", "GET"))
            .andExpect(header().string("Access-Control-Max-Age", "3600"))
            .andExpect(header().string("Access-Control-Allow-Headers", ""));
    }

    @Test
    public void apiPrettyPrint() throws Exception {
        final String indented
            = "{\n"
            + "  \"swagger\" : \"2.0\",\n"
            + "  \"info\" : {\n";

        mvc.perform(request(HttpMethod.GET, API_PATH))
            .andExpect(content().string(startsWith(indented)));
    }

    @Configuration
    @EnableWebMvc
    @Import(SchemaResource.class)
    public static class TestConfiguration {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public MockMvc mvc(final WebApplicationContext context) {
            return MockMvcBuilders.webAppContextSetup(context).build();
        }
    }
}
