package com.adeptia.apigateway;

import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_LIMIT;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_QUOTA;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_REMAINING;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_REMAINING_QUOTA;
import static com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitConstants.HEADER_RESET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestControllerTest {

	private static final String REST_ABOUT = "/rest/about";
	private static final String REST_VERSION = "/rest/version";
	private static final String ADEPTIA_CONNECT = "/aboutinformation";

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void testServiceAdeptia() {

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", "Basic YWRtaW46aW5kaWdvMQ==");
		// requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<>("parameters", requestHeaders);
		ResponseEntity<String> response = restTemplate.exchange(ADEPTIA_CONNECT, HttpMethod.GET, entity, String.class);

		HttpHeaders headers = response.getHeaders();
		String key = "rate-limit-application_serviceAdeptia_127.0.0.1";

		String limit = headers.getFirst(HEADER_LIMIT + key);
		String remaining = headers.getFirst(HEADER_REMAINING + key);
		String reset = headers.getFirst(HEADER_RESET + key);

		assertEquals(limit, "5");
		assertEquals(remaining, "4");
		assertEquals(reset, "60000");

		for (int i = 0; i < 5; i++) {
			response = restTemplate.exchange(ADEPTIA_CONNECT, HttpMethod.GET, entity, String.class);

		}
		System.out.println("----->" + response.getHeaders().getFirst(HEADER_REMAINING + key));
		assertEquals(OK, response.getStatusCode());
	}

	@Test
	public void testServiceAbout() {
		ResponseEntity<String> response = this.restTemplate.getForEntity(REST_ABOUT, String.class);
		HttpHeaders headers = response.getHeaders();
		String key = "rate-limit-application_serviceAbout_127.0.0.1";

		String limit = headers.getFirst(HEADER_LIMIT + key);
		String remaining = headers.getFirst(HEADER_REMAINING + key);
		String reset = headers.getFirst(HEADER_RESET + key);

		assertEquals(limit, "5");
		assertEquals(remaining, "4");
		assertEquals(reset, "60000");

		for (int i = 0; i < 3; i++) {
			response = this.restTemplate.getForEntity(REST_ABOUT, String.class);
		}
		System.out.println("----->" + response.getHeaders().getFirst(HEADER_REMAINING + key));
		assertEquals(OK, response.getStatusCode());
	}

	@Test
	public void testServiceVersion() throws InterruptedException {
		ResponseEntity<String> response = this.restTemplate.getForEntity(REST_VERSION, String.class);
		HttpHeaders headers = response.getHeaders();
		String key = "rate-limit-application_serviceVersion_127.0.0.1";
		assertHeaders(headers, key, false, false);
		assertEquals(OK, response.getStatusCode());

		for (int i = 0; i < 2; i++) {
			response = this.restTemplate.getForEntity(REST_VERSION, String.class);
		}

		headers = response.getHeaders();
		String limit = headers.getFirst(HEADER_LIMIT + key);
		String remaining = headers.getFirst(HEADER_REMAINING + key);
		String reset = headers.getFirst(HEADER_RESET + key);

		assertEquals(limit, "1");
		assertEquals(remaining, "0");
		assertNotEquals(reset, "2000");

		assertEquals(TOO_MANY_REQUESTS, response.getStatusCode());

		TimeUnit.SECONDS.sleep(2);

		response = this.restTemplate.getForEntity(REST_VERSION, String.class);
		headers = response.getHeaders();
		assertHeaders(headers, key, false, false);
		assertEquals(OK, response.getStatusCode());
	}

	private void assertHeaders(HttpHeaders headers, String key, boolean nullable, boolean quotaHeaders) {
		String quota = headers.getFirst(HEADER_QUOTA + key);
		String remainingQuota = headers.getFirst(HEADER_REMAINING_QUOTA + key);
		String limit = headers.getFirst(HEADER_LIMIT + key);
		String remaining = headers.getFirst(HEADER_REMAINING + key);
		String reset = headers.getFirst(HEADER_RESET + key);

		if (nullable) {
			if (quotaHeaders) {
				assertNull(quota);
				assertNull(remainingQuota);
			} else {
				assertNull(limit);
				assertNull(remaining);
			}
			assertNull(reset);
		} else {
			if (quotaHeaders) {
				assertNotNull(quota);
				assertNotNull(remainingQuota);
			} else {
				assertNotNull(limit);
				assertNotNull(remaining);
			}
			assertNotNull(reset);
		}
	}

}