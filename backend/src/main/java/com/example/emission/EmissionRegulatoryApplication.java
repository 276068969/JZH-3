package com.example.emission;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.emission.mapper")
public class EmissionRegulatoryApplication {
  public static void main(String[] args) {
    SpringApplication.run(EmissionRegulatoryApplication.class, args);
  }
}
