package edu.cnm.deepdive.codebreaker.controller;

import com.fasterxml.jackson.annotation.JsonView;
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.CodeService;
import edu.cnm.deepdive.codebreaker.service.GuessService;
import edu.cnm.deepdive.codebreaker.view.GuessView;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/codes/{codeId:" + ValidationPatterns.UUID_PATTERN + "}/guesses")
public class GuessController {

  private final CodeService codeService;
  private final GuessService guessService;

  @Autowired
  public GuessController(CodeService codeService, GuessService guessService) {
    this.codeService = codeService;
    this.guessService = guessService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(GuessView.Flat.class)
  public Iterable<Guess> list(@PathVariable UUID codeId) {
    return codeService
        .get(codeId)
        .map(Code::getGuesses)
        .orElseThrow();
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(GuessView.Flat.class)
  public ResponseEntity<Guess> post(@PathVariable UUID codeId, @Valid @RequestBody Guess guess) {
    return codeService
        .get(codeId)
        .map((code) -> guessService.add(code, guess))
        .map((g) -> ResponseEntity.created(WebMvcLinkBuilder
            .linkTo(WebMvcLinkBuilder
                .methodOn(GuessController.class)
                .get(codeId, g.getId()))
            .toUri()).body(g))
        .orElseThrow();
  }

  @GetMapping(value = ValidationPatterns.UUID_PATH_PARAMETER_PATTERN,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @JsonView(GuessView.Flat.class)
  public Guess get(@PathVariable UUID codeId, @PathVariable UUID id) {
    return codeService
        .get(codeId)
        .flatMap((code) -> guessService.get(id))
        .orElseThrow();
  }

}
