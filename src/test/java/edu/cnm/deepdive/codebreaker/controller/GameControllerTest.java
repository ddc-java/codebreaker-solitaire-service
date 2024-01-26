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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
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
import java.util.HashMap;
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
class GameControllerTest {

  static final String ALL_GAMES_PATH =
      "/{contextPathPart}" + PathComponents.GAMES_COMPONENT;
  static final String GAMES_FILTER_PATH = ALL_GAMES_PATH + "?status={status}";
  static final String SINGLE_GAME_PATH = ALL_GAMES_PATH + "/{gameId}";

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
  GameControllerTest(ObjectMapper objectMapper, GameService gameService, GuessService guessService,
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
  public void postGame_valid() throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("pool", "ABCDEF");
    payload.put("length", 4);
    mockMvc
        .perform(
            post(ALL_GAMES_PATH, contextPathPart)
                .contextPath(contextPath)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(payload))
        )
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.pool", is("ABCDEF")))
        .andExpect(jsonPath("$.length", is(4)))
        .andDo(
            document(
                "games/post-valid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedRequestFields(getPostFields()),
                relaxedResponseFields(getResponseFields())
            )
        );
  }

  @Test
  public void postGame_invalid() throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("pool", "ABCDEF");
    payload.put("length", 0);
    mockMvc
        .perform(
            post(ALL_GAMES_PATH, contextPathPart)
                .contextPath(contextPath)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(payload))
        )
        .andExpect(status().isBadRequest())
        .andExpect(header().doesNotExist("Location"))
        .andExpect(jsonPath("$.status", is(400)))
        .andDo(
            document(
                "games/post-invalid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedRequestFields(getPostFields()),
                relaxedResponseFields(CommonFieldDescriptors.getExceptionFields())
            )
        );
  }

  @Test
  public void getGame_valid() throws Exception {
    Game game = new Game();
    game.setPool("ABCDEF");
    game.setLength(4);
    gameService.add(game);
    String key = stringifier.toString(game.getExternalKey());
    Guess guess = new Guess();
    guess.setText("FEDC");
    guessService.add(game, guess);
    mockMvc
        .perform(
            get(SINGLE_GAME_PATH, contextPathPart, key)
                .contextPath(contextPath)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(key)))
        .andExpect(jsonPath("$.pool", is("ABCDEF")))
        .andExpect(jsonPath("$.length", is(4)))
        .andDo(
            document(
                "games/get-valid",
                preprocessResponse(prettyPrint()),
                pathParameters(getPathVariables()),
                relaxedResponseFields(getResponseFields())
            )
        );
  }

  @Test
  public void getGame_invalid() throws Exception {
    mockMvc
        .perform(
            get(SINGLE_GAME_PATH, contextPathPart, "00000000000000000000000000")
                .contextPath(contextPath)
        )
        .andExpect(status().isNotFound())
        .andDo(document("games/get-invalid"));
  }

  @Test
  public void deleteGame_valid() throws Exception {
    Game game = new Game();
    game.setPool("ABCDEF");
    game.setLength(4);
    gameService.add(game);
    mockMvc
        .perform(
            delete(SINGLE_GAME_PATH, contextPathPart, stringifier.toString(game.getExternalKey()))
                .contextPath(contextPath)
        )
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "games/delete-valid",
                pathParameters(getPathVariables())
            )
        );
  }

  @Test
  public void deleteGame_invalid() throws Exception {
    mockMvc
        .perform(
            get(SINGLE_GAME_PATH, contextPathPart, "00000000000000000000000000")
                .contextPath(contextPath)
        )
        .andExpect(status().isNotFound())
        .andDo(document("games/delete-invalid"));
  }

  static List<ParameterDescriptor> getPathVariables() {
    return List.of(
        parameterWithName("gameId")
            .description("Unique identifier of game resource."),
        parameterWithName("contextPathPart")
            .ignored()
    );
  }

  private static List<ParameterDescriptor> getQueryParameters() {
    return List.of(
        parameterWithName("status")
            .description(
                "Status filter for selecting subset of games: `ALL` (default), `UNSOLVED`, `SOLVED`.")
            .optional()
    );
  }

  private static List<FieldDescriptor> getPostFields() {
    return List.of(
        fieldWithPath("pool")
            .description(
                "Pool of available characters for code and guesses. Duplicated characters will be ignored. Null or unassigned characters (i.e. code point values not mapped to a Unicode character) are not allowed.")
            .type(JsonFieldType.STRING),
        fieldWithPath("length")
            .description("Length (in characters) of generated code. Valid range is 1 to 20.")
            .type(JsonFieldType.NUMBER)
    );
  }

  private static List<FieldDescriptor> getResponseFields() {
    return List.of(
        fieldWithPath("id")
            .description("Unique identifier of the game.")
            .type(JsonFieldType.STRING),
        fieldWithPath("created")
            .description("Timestamp of code creation (start of game).")
            .type(JsonFieldType.STRING),
        fieldWithPath("pool")
            .description("Pool of available characters for code (and guesses).")
            .type(JsonFieldType.STRING),
        fieldWithPath("length")
            .description("Length (in characters) of generated code.")
            .type(JsonFieldType.NUMBER),
        fieldWithPath("solved")
            .description("Flag indicating whether code has been guessed successfully.")
            .type(JsonFieldType.BOOLEAN),
        fieldWithPath("text")
            .description(
                "Text of secret code. This is only included in responses for completed (solved) games.")
            .type(JsonFieldType.STRING)
            .optional(),
        fieldWithPath("guesses")
            .description(
                "Array of guesses submitted in this game.")
            .type("Guess[]")
            .optional()
    );
  }

}
