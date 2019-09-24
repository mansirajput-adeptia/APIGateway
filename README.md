To get started with Zuul, 

    Set up a Zuul Spring Boot project and configure the appropriate Maven dependences. 
    Modify the Spring Boot project with Spring Cloud annotations to tell it that it will be a Zuul service. 
    Configure Zuul to communicate with Eureka (optional)

Usage

Add the dependency on pom.xml

<dependency>
    <groupId>com.marcosbarbero.cloud</groupId>
    <artifactId>spring-cloud-zuul-ratelimit</artifactId>
    <version>LATEST</version>
</dependency>

How to add rate limiting to Zuul

Configure the rates for each endpoint that you want to control. This can be done via either properties file or yml configuration. Sample yml file can be seen below. 

zuul:
  ratelimit:
    key-prefix: your-prefix
    enabled: true
    repository: REDIS
    behind-proxy: true
    add-response-headers: true
    default-policy-list: #optional - will apply unless specific policy exists
      - limit: 10 #optional - request number limit per refresh interval window
        quota: 1000 #optional - request time limit per refresh interval window (in seconds)
        refresh-interval: 60 #default value (in seconds)
        type: #optional
          - user
          - origin
          - url
          - httpmethod

When one of the endpoints reached their configured rate limit, it will automatically respond with http error code 429 Too Many Requests for subsequent requests until the rate limit comes back to the allowed range. This will prevent the underlying service from being overload in a peak time or under a DoS attack.
