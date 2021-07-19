# Twintip Schema Discovery for Spring Web

[![Build Status](https://img.shields.io/travis/zalando-stups/twintip-spring-web/master.svg)](https://travis-ci.org/zalando-stups/twintip-spring-web)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/org.zalando/twintip-spring-web/badge.svg)](http://www.javadoc.io/doc/org.zalando/twintip-spring-web)
[![Release](https://img.shields.io/github/release/zalando-stups/twintip-spring-web.svg)](https://github.com/zalando-stups/twintip-spring-web/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/twintip-spring-web.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/twintip-spring-web)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/zalando-stups/twintip-spring-web/master/LICENSE)

Twintipify your Spring Web MVC application. This library exposes your API schema at `/.well-known/schema-discovery` as specified in our [RESTful API Guidelines](http://zalando.github.io/restful-api-guidelines/#192). *Twintip* was the former API crawler that was tasked with finding and indexing API schemas, hence the name of this library.

## Features

- never think about your Twintip-compatible schema discovery endpoint ever again

## Dependencies

- Java 11
- Any build tool using Maven Central, or direct download
- Servlet Container
- Spring

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>twintip-spring-web</artifactId>
    <version>${twintip-spring-web.version}</version>
</dependency>
```

## Configuration

First make sure that the endpoints are mapped into your application.

```java
import org.zalando.twintip.spring.SchemaResource;

@Configuration
@Import(SchemaResource.class)
public class YourConfigration {
    // whatever you configure
}
```

Next you need to provide the API definition and decide where clients should find it.
You can do this in your *application.properties* or 
[*application.yml*](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-yaml).

```yaml
twintip:
  mapping: /api
  yaml: "classpath:api.yml"
```

### CORS Support

SchemaResource will send CORS Access-Control-* headers by default. You can disable this by setting `twintip.cors`
property to false.

Host, port, schemes and base path inside of the API definition can be overridden by setting the `twintip.baseUrl` 
property to full base URL of API.

## Getting Help

If you have questions, concerns, bug reports, etc., please file an issue in this repository's [Issue Tracker](../../issues).

## Getting Involved/Contributing

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change. For
more details, check the [contribution guidelines](CONTRIBUTING.md).
