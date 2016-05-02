# Twintip Schema Discovery for Spring Web

[![Build Status](https://img.shields.io/travis/zalando/twintip-spring-web.svg)](https://travis-ci.org/zalando/twintip-spring-web)
[![Coverage Status](https://img.shields.io/coveralls/zalando/twintip-spring-web.svg)](https://coveralls.io/r/zalando/twintip-spring-web)
[![Release](https://img.shields.io/github/release/zalando/twintip-spring-web.svg)](https://github.com/zalando/twintip-spring-web/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/twintip-spring-web.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/twintip-spring-web)

Twintipify your Spring Web MVC application. This library exposes all the endpoints TWINTIP requires.
See the documentation for [TWINTIP](http://stups.readthedocs.org/en/latest/components/twintip.html).

## Usage

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
You can do this in your *application.properties* or [*application.yml*](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-yaml).

```yaml
twintip:
  mapping: /api
  yaml: "classpath:api.yml"
```

You are done.

## Other options

SchemaResource will send CORS Access-Control-* headers by default. You can disable this by setting twintip.cors property to false.

## License

Copyright [2015] Zalando SE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
