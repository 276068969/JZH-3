package com.example.emission.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {})
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login").permitAll()
            .requestMatchers(HttpMethod.GET, "/announcements").permitAll()
            .requestMatchers(HttpMethod.GET, "/announcements/list").permitAll()
            .requestMatchers(HttpMethod.GET, "/announcements/detail").permitAll()
            .requestMatchers(HttpMethod.GET, "/vehicles/search").permitAll()
            .requestMatchers(HttpMethod.GET, "/dashboard").hasAnyRole("平台管理员", "监管人员", "检测站工作人员")
            .requestMatchers(HttpMethod.GET, "/stations", "/stations/status").hasAnyRole("平台管理员", "监管人员")
            .requestMatchers(HttpMethod.GET, "/inspections/**").hasAnyRole("平台管理员", "监管人员", "检测站工作人员")
            .requestMatchers(HttpMethod.POST, "/inspections/audit").hasAnyRole("平台管理员", "监管人员")
            .requestMatchers(HttpMethod.POST, "/inspections/create").hasAnyRole("平台管理员", "检测站工作人员")
            .requestMatchers(HttpMethod.POST, "/inspections/judge").hasAnyRole("平台管理员", "监管人员", "检测站工作人员")
            .requestMatchers(HttpMethod.GET, "/warnings", "/warnings/detail", "/warnings/inspections").hasAnyRole("平台管理员", "监管人员")
            .requestMatchers(HttpMethod.POST, "/warnings/handle").hasAnyRole("平台管理员", "监管人员")
            .requestMatchers(HttpMethod.GET, "/pollutant-limit-rules/**").hasAnyRole("平台管理员", "监管人员")
            .requestMatchers(HttpMethod.POST, "/pollutant-limit-rules/create",
                "/pollutant-limit-rules/update", "/pollutant-limit-rules/delete").hasRole("平台管理员")
            .requestMatchers(HttpMethod.POST, "/announcements/create",
                "/announcements/update", "/announcements/delete",
                "/announcements/publish", "/announcements/offline").hasRole("平台管理员")
            .requestMatchers("/auth/me").authenticated()
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.setCharacterEncoding("UTF-8");
              objectMapper.writeValue(response.getOutputStream(),
                  Map.of("success", false, "code", "UNAUTHORIZED", "message", "未登录或登录已过期"));
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.setCharacterEncoding("UTF-8");
              objectMapper.writeValue(response.getOutputStream(),
                  Map.of("success", false, "code", "FORBIDDEN", "message", "权限不足，无法访问该资源"));
            })
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
