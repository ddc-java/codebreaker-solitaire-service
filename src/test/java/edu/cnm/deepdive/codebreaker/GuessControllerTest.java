package edu.cnm.deepdive.codebreaker;

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
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.CodeService;
import edu.cnm.deepdive.codebreaker.service.GuessService;
import edu.cnm.deepdive.codebreaker.service.UUIDStringifier;
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

  private final ObjectMapper objectMapper;
  private final CodeService codeService;
  private final GuessService guessService;

  @Value("${rest-docs.scheme}")
  private String docScheme;

  @Value("${rest-docs.host}")
  private String docHost;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  private String contextPathPart;
  private MockMvc mockMvc;

  @Autowired
  GuessControllerTest(
      ObjectMapper objectMapper, CodeService codeService, GuessService guessService) {
    this.objectMapper = objectMapper;
    this.codeService = codeService;
    this.guessService = guessService;
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

  @AfterEach
  public void tearDown(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    codeService.clear();
  }

  @Test
  public void postGuess_valid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(6);
    code.setText("ABACAB");
    codeService.add(code);
    Map<String, String> guessSkeleton = Map.of("text", "AABBCC");
    mockMvc.perform(
        post("/{contextPathPart}/codes/{codeId}/guesses", contextPathPart, code.getKey())
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
                "guess/post-valid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(getCodePathVariables()),
                relaxedRequestFields(getPostRequestFields()),
                relaxedResponseFields(getFlatFields())
            )
        );
  }

  @Test
  public void postGuess_invalid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    Map<String, String> guessSkeleton = Map.of("text", "AAA");
    mockMvc.perform(
        post("/{contextPathPart}/codes/{codeId}/guesses", contextPathPart, code.getKey())
            .contextPath(contextPath)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(guessSkeleton))
    )
        .andExpect(status().isBadRequest())
        .andExpect(header().doesNotExist("Location"))
        .andExpect(jsonPath("$.status", is(400)))
        .andDo(
            document(
                "guess/post-invalid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(getCodePathVariables()),
                relaxedRequestFields(getPostRequestFields()),
                relaxedResponseFields(CommonFieldDescriptors.getExceptionFields())
            )
        );
  }

  @Test
  public void listGuesses_valid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(6);
    codeService.add(code);
    for (String text : new String[]{"AAAAAA", "BBBBBB", "CCCCCC"}) {
      Guess guess = new Guess();
      guess.setText(text);
      guessService.add(code, guess);
    }
    mockMvc.perform(
        get("/{contextPathPart}/codes/{codeId}/guesses", contextPathPart, code.getKey())
            .contextPath(contextPath)
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(3)))
        .andDo(
            document(
                "guess/list-valid",
                preprocessResponse(prettyPrint()),
                pathParameters(getCodePathVariables())
            )
        );
  }

  @Test
  public void getGuess_valid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    Guess guess = new Guess();
    guess.setCode(code);
    guess.setText("AAAA");
    guessService.add(code, guess);
    mockMvc.perform(
        get("/{contextPathPart}/codes/{codeId}/guesses/{guessId}",
            contextPathPart, code.getKey(), guess.getKey())
            .contextPath(contextPath)
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(guess.getKey())))
        .andExpect(jsonPath("$.text", is("AAAA")))
        .andDo(
            document(
                "guess/get-valid",
                preprocessResponse(prettyPrint()),
                pathParameters(getGuessPathVariables()),
                relaxedResponseFields(getFlatFields())
            )
        );
  }

  @Test
  public void getGuess_invalid() throws Exception {
    mockMvc.perform(
        get("/{contextPathPart}/codes/00000000000000000000000000/guesses/00000000000000000000000000",
            contextPathPart)
            .contextPath(contextPath)
    )
        .andExpect(status().isNotFound())
        .andDo(document("guess/get-invalid"));
  }

  private List<ParameterDescriptor> getCodePathVariables() {
    return List.of(
        parameterWithName("codeId")
            .description("Unique identifier of code."),
        parameterWithName("contextPathPart")
            .ignored()
    );
  }

  private List<ParameterDescriptor> getGuessPathVariables() {
    List<ParameterDescriptor> fields = new LinkedList<>(getCodePathVariables());
    Collections.addAll(
        fields,
        parameterWithName("guessId")
            .description("Unique identifier of guess.")
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
            .description("Count of characters in the guess that are in the same positions in the code.")
            .type(JsonFieldType.NUMBER),
        fieldWithPath("nearMatches")
            .description("Count of characters in the guess that are in code, but not in the same positions.")
            .type(JsonFieldType.NUMBER),
        fieldWithPath("solution")
            .description("Flag indicating whether this guess exacly matches the code.")
            .type(JsonFieldType.BOOLEAN)
    );
  }

}
