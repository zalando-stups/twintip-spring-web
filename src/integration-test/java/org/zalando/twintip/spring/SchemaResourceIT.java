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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class SchemaResourceIT {

    private static final String API_PATH = "/api";
    private static final String UI_PATH = "/ui";
    public static final String YAML = "field: \"value\"\ncol: \n    - 1\n    - 2\n    - 3";

    @Autowired
    private MockMvc mvc;

    @BeforeClass
    public static void initialize() {
        System.setProperty("twintip.mapping", API_PATH);
    }

    @Before
    public void setUp() {
        System.setProperty("twintip.mapping", API_PATH);
        System.clearProperty("twintip.type");
        System.clearProperty("twintip.ui");
    }

    @Test
    public void schemaDiscoveryWithDefaults() throws Exception {
        mvc.perform(request(HttpMethod.GET, SchemaResource.SCHEMA_DISCOVERY_MAPPING))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.schema_url", is(API_PATH)))
                .andExpect(jsonPath("$.schema_type", is("swagger-2.0")))
                .andExpect(jsonPath("$", not(hasKey("ui_url"))));
    }

    @Test
    public void schemaDiscoveryWithUi() throws Exception {
        System.setProperty("twintip.ui", UI_PATH);
        System.setProperty("twintip.type", "swagger-3.0");

        mvc.perform(request(HttpMethod.GET, SchemaResource.SCHEMA_DISCOVERY_MAPPING))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.schema_url", is(API_PATH)))
                .andExpect(jsonPath("$.ui_url", is(UI_PATH)))
                .andExpect(jsonPath("$.schema_type", is("swagger-3.0")));
    }

    @Test
    public void apiWithoutAcceptAsJson() throws Exception {
        mvc.perform(request(HttpMethod.GET, API_PATH))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.field", is("value")))
                .andExpect(jsonPath("$.col", hasSize(3)));
    }

    @Test
    public void apiWithAcceptAsJson() throws Exception {
        mvc.perform(request(HttpMethod.GET, API_PATH)
                .accept(APPLICATION_JSON))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.field", is("value")));
    }

    @Test
    public void apiWithAcceptAsYaml() throws Exception {
        final MediaType yaml = MediaType.parseMediaType("application/yaml");

        mvc.perform(request(HttpMethod.GET, API_PATH)
                .accept(yaml))
                .andExpect(content().contentType(yaml))
                .andExpect(content().string(YAML));
    }

    @EnableWebMvc
    @Configuration
    public static class TestConfiguration {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public SchemaResource resource() throws IOException {
            return new SchemaResource(new ByteArrayResource(YAML.getBytes()));
        }

        @Bean
        public MockMvc mvc(final WebApplicationContext context) {
            return MockMvcBuilders.webAppContextSetup(context).build();
        }
    }

}
