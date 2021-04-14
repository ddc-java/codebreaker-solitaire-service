package edu.cnm.deepdive.codebreaker.controller;

import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.CodeService;
import edu.cnm.deepdive.codebreaker.service.GuessService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/codes/{codeId:" + ValidationPatterns.ID_PATTERN + "}/guesses")
@CrossOrigin(origins = "https://www.webtools.services")
public class GuessController {

  private final CodeService codeService;
  private final GuessService guessService;

  @Autowired
  public GuessController(CodeService codeService, GuessService guessService) {
    this.codeService = codeService;
    this.guessService = guessService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Guess> list(@PathVariable String codeId) {
    return codeService
        .get(codeId)
        .map(Code::getGuesses)
        .orElseThrow();
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Guess> post(@PathVariable String codeId, @Valid @RequestBody Guess guess) {
    return codeService
        .get(codeId)
        .map((code) -> guessService.add(code, guess))
        .map((g) -> ResponseEntity.created(WebMvcLinkBuilder
            .linkTo(WebMvcLinkBuilder
                .methodOn(GuessController.class)
                .get(codeId, g.getKey()))
            .toUri()).body(g))
        .orElseThrow();
  }

  @GetMapping(value = ValidationPatterns.ID_PATH_PARAMETER_PATTERN,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Guess get(@PathVariable String codeId, @PathVariable String id) {
    return codeService
        .get(codeId)
        .flatMap((code) -> guessService.get(id))
        .orElseThrow();
  }

}
