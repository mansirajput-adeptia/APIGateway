package com.adeptia.apigateway;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rest")
public class RestController {

	@GetMapping("/about")
	public ResponseEntity<String> getSimple() {
		return ResponseEntity.ok("This is adeptia API Gateway.");
	}

	@GetMapping("/version")
	public ResponseEntity<String> getAdvanced() {
		return ResponseEntity.ok("version:1.0.0.0");
	}
}