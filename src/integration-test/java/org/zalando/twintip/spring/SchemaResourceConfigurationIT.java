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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.hasItems;
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
    "twintip.mapping=" + SchemaResourceConfigurationIT.API_PATH,
    "twintip.yaml=classpath:/petstore.yml",
    "twintip.ui=" + SchemaResourceConfigurationIT.UI_PATH,
    "twintip.type=swagger-3.0",
    "twintip.cors=false",
    "twintip.baseUrl=https://example.com/example-api",
})
public class SchemaResourceConfigurationIT {

    static final String API_PATH = "/super-api";
    static final String UI_PATH = "/ui";

    @Autowired
    private MockMvc mvc;

    @Test
    public void schemaDiscoveryWithUi() throws Exception {
        mvc.perform(request(HttpMethod.GET, SchemaResource.SCHEMA_DISCOVERY_MAPPING))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.schema_url", is(API_PATH)))
            .andExpect(jsonPath("$.ui_url", is(UI_PATH)))
            .andExpect(jsonPath("$.schema_type", is("swagger-3.0")));
    }

    @Test
    public void apiWithCorsDisabled() throws Exception {
        mvc.perform(request(HttpMethod.GET, API_PATH))
            .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
            .andExpect(header().doesNotExist("Access-Control-Allow-Methods"))
            .andExpect(header().doesNotExist("Access-Control-Max-Age"))
            .andExpect(header().doesNotExist("Access-Control-Allow-Headers"));
    }

    @Test
    public void apiWithBaseUrl() throws Exception {
        mvc.perform(request(HttpMethod.GET, API_PATH))
            .andExpect(jsonPath("$.host", is("example.com")))
            .andExpect(jsonPath("$.basePath", is("/example-api")))
            .andExpect(jsonPath("$.schemes", hasSize(1)))
            .andExpect(jsonPath("$.schemes", hasItems("https")));
    }

    @EnableWebMvc
    @Configuration
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
