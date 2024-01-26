/*
 *  Copyright 2024 CNM Ingenuity, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.codebreaker.controller;

import static org.hamcrest.core.Is.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.codebreaker.CodebreakerApplication;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.GameService;
import edu.cnm.deepdive.codebreaker.service.GuessService;
import edu.cnm.deepdive.codebreaker.view.UUIDStringifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(classes = CodebreakerApplication.class)
class GuessControllerTest {

  private static final String ALL_GUESSES_PATH =
      GameControllerTest.ALL_GAMES_PATH + "/{gameId}/guesses";
  private static final String SINGLE_GUESS_PATH = ALL_GUESSES_PATH + "/{guessId}";

  private final ObjectMapper objectMapper;
  private final GameService gameService;
  private final GuessService guessService;
  private final UUIDStringifier stringifier;

  @Value("${rest-docs.scheme}")
  private String docScheme;

  @Value("${rest-docs.host}")
  private String docHost;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  private String contextPathPart;
  private MockMvc mockMvc;

  @Autowired
  GuessControllerTest(ObjectMapper objectMapper, GameService gameService, GuessService guessService,
      UUIDStringifier stringifier) {
    this.objectMapper = objectMapper;
    this.gameService = gameService;
    this.guessService = guessService;
    this.stringifier = stringifier;
  }

  @BeforeEach
  public void setup(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    contextPathPart = contextPath.startsWith("/") ? contextPath.substring(1) : contextPath;
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(
            documentationConfiguration(restDocumentation)
                .uris()
                .withScheme(docScheme)
                .withHost(docHost)
                .withPort(443)
        )
        .build();
  }

  @SuppressWarnings("unused")
  @AfterEach
  public void tearDown(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    gameService.clear();
  }

  @Test
  public void postGuess_valid() throws Exception {
    Game game = new Game();
    game.setPool("ABCDEF");
    game.setLength(6);
    game.setText("ABACAB");
    gameService.add(game);
    Map<String, String> guessSkeleton = Map.of("text", "AABBCC");
    mockMvc
        .perform(
            post(ALL_GUESSES_PATH, contextPathPart, stringifier.toString(game.getExternalKey()))
                .contextPath(contextPath)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(guessSkeleton))
        )
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.exactMatches", is(1)))
        .andExpect(jsonPath("$.nearMatches", is(4)))
        .andDo(
            document(
                "guesses/post-valid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(GameControllerTest.getPathVariables()),
                relaxedRequestFields(getPostRequestFields()),
                relaxedResponseFields(getFlatFields())
            )
        );
  }

  @Test
  public void postGuess_invalid() throws Exception {
    Game game = new Game();
    game.setPool("ABCDEF");
    game.setLength(4);
    gameService.add(game);
    Map<String, String> guessSkeleton = Map.of("text", "AAA");
    mockMvc
        .perform(
            post(ALL_GUESSES_PATH, contextPathPart, stringifier.toString(game.getExternalKey()))
                .contextPath(contextPath)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(guessSkeleton))
        )
        .andExpect(status().isBadRequest())
        .andExpect(header().doesNotExist("Location"))
        .andExpect(jsonPath("$.status", is(400)))
        .andDo(
            document(
                "guesses/post-invalid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(GameControllerTest.getPathVariables()),
                relaxedRequestFields(getPostRequestFields()),
                relaxedResponseFields(CommonFieldDescriptors.getExceptionFields())
            )
        );
  }

  @Test
  public void listGuesses_valid() throws Exception {
    Game game = new Game();
    game.setPool("ABCDEF");
    game.setLength(6);
    gameService.add(game);
    for (String text : new String[]{"AAAAAA", "BBBBBB", "CCCCCC"}) {
      Guess guess = new Guess();
      guess.setText(text);
      guessService.add(game, guess);
    }
    mockMvc
        .perform(
            get(ALL_GUESSES_PATH, contextPathPart, stringifier.toString(game.getExternalKey()))
                .contextPath(contextPath)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(3)))
        .andDo(
            document(
                "guesses/list-valid",
                preprocessResponse(prettyPrint()),
                pathParameters(GameControllerTest.getPathVariables())
            )
        );
  }

  @Test
  public void getGuess_valid() throws Exception {
    Game game = new Game();
    game.setPool("ABCDEF");
    game.setLength(4);
    gameService.add(game);
    Guess guess = new Guess();
    guess.setGame(game);
    guess.setText("AAAA");
    guessService.add(game, guess);
    String key = stringifier.toString(guess.getExternalKey());
    mockMvc
        .perform(
            get(SINGLE_GUESS_PATH,
                contextPathPart, stringifier.toString(game.getExternalKey()), key)
                .contextPath(contextPath)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(key)))
        .andExpect(jsonPath("$.text", is("AAAA")))
        .andDo(
            document(
                "guesses/get-valid",
                preprocessResponse(prettyPrint()),
                pathParameters(getPathVariables()),
                relaxedResponseFields(getFlatFields())
            )
        );
  }

  @Test
  public void getGuess_invalid() throws Exception {
    mockMvc
        .perform(
            get(SINGLE_GUESS_PATH,
                contextPathPart, "00000000000000000000000000", "00000000000000000000000000")
                .contextPath(contextPath)
        )
        .andExpect(status().isNotFound())
        .andDo(document("guesses/get-invalid"));
  }

  private List<ParameterDescriptor> getPathVariables() {
    List<ParameterDescriptor> fields = new LinkedList<>(GameControllerTest.getPathVariables());
    Collections.addAll(
        fields,
        parameterWithName("guessId")
            .description("Unique identifier of guess resource.")
    );
    return fields;
  }

  private List<FieldDescriptor> getPostRequestFields() {
    return List.of(
        fieldWithPath("text")
            .description("Guess of code text.")
            .type(JsonFieldType.STRING)
    );
  }

  private List<FieldDescriptor> getFlatFields() {
    return List.of(
        fieldWithPath("id")
            .description("Unique identifier of the submitted guess.")
            .type(JsonFieldType.STRING),
        fieldWithPath("created")
            .description("Timestamp of guess submission.")
            .type(JsonFieldType.STRING),
        fieldWithPath("text")
            .description("Text of guess.")
            .type(JsonFieldType.STRING),
        fieldWithPath("exactMatches")
            .description(
                "Count of characters in the guess that are in the same positions in the secret code.")
            .type(JsonFieldType.NUMBER),
        fieldWithPath("nearMatches")
            .description(
                "Count of characters in the guess that are in secret code, but not in the same positions.")
            .type(JsonFieldType.NUMBER),
        fieldWithPath("solution")
            .description("Flag indicating whether this guess exacly matches the secret code.")
            .type(JsonFieldType.BOOLEAN)
    );
  }

}
