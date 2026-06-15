package com.example.emission.controller;

import com.example.emission.dto.LoginRequest;
import com.example.emission.dto.LoginResponse;
import com.example.emission.model.UserAccount;
import com.example.emission.service.DemoDataService;
import com.example.emission.service.JwtService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final DemoDataService demoDataService;
  private final JwtService jwtService;

  public AuthController(DemoDataService demoDataService, JwtService jwtService) {
    this.demoDataService = demoDataService;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return demoDataService.findUser(request.username(), request.password())
        .map(user -> ResponseEntity.ok(new LoginResponse(jwtService.createToken(user), user)))
        .orElseGet(() -> ResponseEntity.status(401).build());
  }

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false, "message", "未登录或登录已过期"));
    }
    UserAccount user = (UserAccount) authentication.getDetails();
    return ResponseEntity.ok(Map.of(
        "success", true,
        "user", user
    ));
  }
}
