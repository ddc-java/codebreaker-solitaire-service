package edu.cnm.deepdive.codebreaker.controller;

import com.fasterxml.jackson.annotation.JsonView;
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.service.CodeService;
import edu.cnm.deepdive.codebreaker.view.CodeView;
import java.net.URI;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/codes")
public class CodeController {

  private final CodeService codeService;

  public CodeController(CodeService codeService) {
    this.codeService = codeService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(CodeView.Flat.class)
  public Iterable<Code> list() {
    return codeService.getAll();
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(CodeView.Flat.class)
  public ResponseEntity<Code> post(@Valid @RequestBody Code code) {
    code = codeService.add(code);
    URI location = WebMvcLinkBuilder
        .linkTo(WebMvcLinkBuilder
            .methodOn(CodeController.class)
            .get(code.getId()))
        .toUri();
    return ResponseEntity.created(location).body(code);
  }

  @GetMapping(value = ValidationPatterns.UUID_PATH_PARAMETER_PATTERN,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(CodeView.Hierarchical.class)
  public Code get(@PathVariable UUID id) {
    return codeService
        .get(id)
        .orElseThrow();
  }

  @DeleteMapping(value = ValidationPatterns.UUID_PATH_PARAMETER_PATTERN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    codeService
        .get(id)
        .stream()
        .peek(codeService::remove)
        .findFirst()
        .orElseThrow();
  }

}
