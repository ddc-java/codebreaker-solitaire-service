package edu.cnm.deepdive.codebreaker.controller;

import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.service.CodeService;
import java.net.URI;
import javax.validation.Valid;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/codes")
@CrossOrigin(origins = "https://www.webtools.services")
public class CodeController {

  private final CodeService codeService;

  public CodeController(CodeService codeService) {
    this.codeService = codeService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Code> list(@RequestParam(required = false, defaultValue = "all") String status) {
    return codeService.list(status);
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Code> post(@Valid @RequestBody Code code) {
    code = codeService.add(code);
    URI location = WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(CodeController.class)
            .get(code.getKey()))
        .toUri();
    return ResponseEntity.created(location).body(code);
  }

  @GetMapping(value = ValidationPatterns.ID_PATH_PARAMETER_PATTERN,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Code get(@PathVariable String id) {
    return codeService
        .get(id)
        .orElseThrow();
  }

  @DeleteMapping(value = ValidationPatterns.ID_PATH_PARAMETER_PATTERN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    codeService
        .get(id)
        .stream()
        .peek(codeService::remove)
        .findFirst()
        .orElseThrow();
  }

}
