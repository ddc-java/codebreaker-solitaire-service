package edu.cnm.deepdive.codebreaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;

@SpringBootApplication
public class CodebreakerApplication {

  public static void main(String[] args) {
    SpringApplication.run(CodebreakerApplication.class, args);
  }

}
