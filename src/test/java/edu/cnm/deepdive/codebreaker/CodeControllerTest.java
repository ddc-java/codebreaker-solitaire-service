package edu.cnm.deepdive.codebreaker;

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
import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.CodeService;
import edu.cnm.deepdive.codebreaker.service.GuessService;
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
class CodeControllerTest {

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
  CodeControllerTest(
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
  public void postCode_valid() throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("pool", "ABCDEF");
    payload.put("length", 4);
    mockMvc.perform(
        post("/{contextPathPart}/codes", contextPathPart)
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
                "code/post-valid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedRequestFields(getPostFields()),
                relaxedResponseFields(getFlatFields())
            )
        );
  }

  @Test
  public void postCode_invalid() throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("pool", "ABCDEF");
    payload.put("length", 0);
    mockMvc.perform(
        post("/{contextPathPart}/codes", contextPathPart)
            .contextPath(contextPath)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(payload))
    )
        .andExpect(status().isBadRequest())
        .andExpect(header().doesNotExist("Location"))
        .andExpect(jsonPath("$.status", is(400)))
        .andDo(
            document(
                "code/post-invalid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedRequestFields(getPostFields()),
                relaxedResponseFields(CommonFieldDescriptors.getExceptionFields())
            )
        );
  }

  @Test
  public void listCodes_all() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    code = new Code();
    code.setPool("0123456789");
    code.setLength(5);
    codeService.add(code);
    code = new Code();
    code.setPool("ROYGBIV");
    code.setLength(6);
    codeService.add(code);
    Guess guess = new Guess();
    guess.setText(code.getText());
    guessService.add(code, guess);
    mockMvc.perform(
        get("/{contextPathPart}/codes", contextPathPart)
            .contextPath(contextPath)
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(3)))
        .andDo(
            document(
                "code/list-all",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedRequestParameters(getQueryParameters())
            )
        );
  }

  @Test
  public void listCodes_unsolved() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    code = new Code();
    code.setPool("0123456789");
    code.setLength(5);
    codeService.add(code);
    code = new Code();
    code.setPool("ROYGBIV");
    code.setLength(6);
    codeService.add(code);
    Guess guess = new Guess();
    guess.setText(code.getText());
    guessService.add(code, guess);
    mockMvc.perform(
        get("/{contextPathPart}/codes?status=UNSOLVED", contextPathPart)
            .contextPath(contextPath)
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andDo(
            document(
                "code/list-unsolved",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedRequestParameters(getQueryParameters())
            )
        );
  }

  @Test
  public void listCodes_solved() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    code = new Code();
    code.setPool("0123456789");
    code.setLength(5);
    codeService.add(code);
    code = new Code();
    code.setPool("ROYGBIV");
    code.setLength(6);
    codeService.add(code);
    Guess guess = new Guess();
    guess.setText(code.getText());
    guessService.add(code, guess);
    mockMvc.perform(
        get("/{contextPathPart}/codes?status=SOLVED", contextPathPart)
            .contextPath(contextPath)
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(1)))
        .andDo(
            document(
                "code/list-solved",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                relaxedRequestParameters(getQueryParameters())
            )
        );
  }

  @Test
  public void getCode_valid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    mockMvc.perform(
        get("/{contextPathPart}/codes/{codeId}", contextPathPart, code.getKey())
            .contextPath(contextPath)
    )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pool", is("ABCDEF")))
        .andExpect(jsonPath("$.length", is(4)))
        .andDo(
            document(
                "code/get-valid",
                preprocessResponse(prettyPrint()),
                pathParameters(getPathVariables()),
                relaxedResponseFields(getFlatFields())
            )
        );
  }

  @Test
  public void getCode_invalid() throws Exception {
    mockMvc.perform(
        get("/{contextPathPart}/codes/00000000000000000000000000", contextPathPart)
            .contextPath(contextPath)
    )
        .andExpect(status().isNotFound())
        .andDo(document("code/get-invalid"));
  }

  @Test
  public void deleteCode_valid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    mockMvc.perform(
        delete("/{contextPathPart}/codes/{codeId}", contextPathPart, code.getKey())
            .contextPath(contextPath)
    )
        .andExpect(status().isNoContent())
        .andDo(
            document(
                "code/delete-valid",
                pathParameters(getPathVariables())
            )
        );
  }

  @Test
  public void deleteCode_invalid() throws Exception {
    mockMvc.perform(
        delete("/{contextPathPart}/codes/00000000000000000000000000", contextPathPart)
            .contextPath(contextPath)
    )
        .andExpect(status().isNotFound())
        .andDo(document("code/delete-invalid"));
  }

  private List<ParameterDescriptor> getPathVariables() {
    return List.of(
        parameterWithName("codeId")
            .description("Unique identifier of code."),
        parameterWithName("contextPathPart")
            .ignored()
    );
  }

  private List<ParameterDescriptor> getQueryParameters() {
    return List.of(
        parameterWithName("status")
            .description(
                "Status filter for selecting subset of codes: `ALL` (default), `UNSOLVED`, `SOLVED`.")
            .optional()
    );
  }

  private List<FieldDescriptor> getPostFields() {
    return List.of(
        fieldWithPath("pool")
            .description(
                "Pool of available characters for code. Duplicated characters will be ignored. Null characters are not allowed.")
            .type(JsonFieldType.STRING),
        fieldWithPath("length")
            .description("Length (in characters) of generated code. Valid range is 1 to 20.")
            .type(JsonFieldType.NUMBER)
    );
  }

  private List<FieldDescriptor> getFlatFields() {
    return List.of(
        fieldWithPath("id")
            .description("Unique identifier of the generated code.")
            .type(JsonFieldType.STRING),
        fieldWithPath("created")
            .description("Timestamp of code creation.")
            .type(JsonFieldType.STRING),
        fieldWithPath("pool")
            .description("Pool of available characters for code.")
            .type(JsonFieldType.STRING),
        fieldWithPath("length")
            .description("Length (in characters) of generated code.")
            .type(JsonFieldType.NUMBER),
        fieldWithPath("guessCount")
            .description("Number of guesses submitted for this code.")
            .type(JsonFieldType.NUMBER),
        fieldWithPath("solved")
            .description("Flag indicating whether code has been guessed successfully.")
            .type(JsonFieldType.BOOLEAN),
        fieldWithPath("href")
            .description(
                "URL of code resource, usable in HTTP GET requests.")
            .type(JsonFieldType.STRING),
        fieldWithPath("text")
            .description(
                "Text of secret code. This is only included for codes that have been solved.")
            .type(JsonFieldType.STRING)
            .optional()
    );
  }

}
