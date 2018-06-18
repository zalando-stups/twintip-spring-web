package org.zalando.twintip.spring;

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
    "twintip.mapping=" + SchemaResourceConfigurationTest.API_PATH,
    "twintip.yaml=classpath:/petstore.yml",
    "twintip.ui=" + SchemaResourceConfigurationTest.UI_PATH,
    "twintip.type=swagger-3.0",
    "twintip.cors=false",
    "twintip.baseUrl=https://example.com:8080/example-api",
})
public class SchemaResourceConfigurationTest {

    static final String API_PATH = "/super-api";
    static final String UI_PATH = "/ui";

    @Autowired
    private MockMvc mvc;

    @Test
    public void schemaDiscoveryWithUi() throws Exception {
        mvc.perform(request(HttpMethod.GET, SchemaResource.SCHEMA_DISCOVERY_MAPPING))
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
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
            .andExpect(jsonPath("$.host", is("example.com:8080")))
            .andExpect(jsonPath("$.basePath", is("/example-api")))
            .andExpect(jsonPath("$.schemes", hasSize(1)))
            .andExpect(jsonPath("$.schemes", hasItems("https")));
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
