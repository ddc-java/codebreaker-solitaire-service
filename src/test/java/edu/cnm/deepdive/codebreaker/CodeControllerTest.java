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
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.service.CodeService;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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

  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private CodeService codeService;

  @BeforeEach
  public void setup(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(documentationConfiguration(restDocumentation))
        .build();
  }

  @AfterEach
  public void tearDown(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    codeService.clear();
  }

  @Test
  public void postCode_valid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    mockMvc.perform(
        post("/codes")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(code))
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
    Code code = new Code();
    code.setPool("ABCDEF");
    mockMvc.perform(
        post("/codes")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(code))
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
  public void listCodes_valid() throws Exception {
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
    mockMvc.perform(get("/codes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(3)))
        .andDo(
            document(
                "code/list-valid",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
            )
        );
  }

  @Test
  public void getCode_valid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    mockMvc.perform(get("/codes/{id}", code.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pool", is("ABCDEF")))
        .andExpect(jsonPath("$.length", is(4)))
        .andDo(
            document(
                "code/get-valid",
                preprocessResponse(prettyPrint()),
                pathParameters(getPathVariables()),
                relaxedResponseFields(getHierarchicalFields())
            )
        );
  }

  @Test
  public void getCode_invalid() throws Exception {
    mockMvc.perform(get("/codes/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound())
        .andDo(document("code/get-invalid"));
  }

  @Test
  public void deleteCode_valid() throws Exception {
    Code code = new Code();
    code.setPool("ABCDEF");
    code.setLength(4);
    codeService.add(code);
    mockMvc.perform(delete("/codes/{id}", code.getId()))
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
    mockMvc.perform(delete("/codes/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound())
        .andDo(document("code/delete-invalid"));
  }

  private List<ParameterDescriptor> getPathVariables() {
    return List.of(
        parameterWithName("id")
            .description("Unique identifier of code.")
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
        fieldWithPath("text")
            .description("Actual code text. This is only included for codes that have been solved.")
            .type(JsonFieldType.STRING)
            .optional()
    );
  }

  private List<FieldDescriptor> getHierarchicalFields() {
    List<FieldDescriptor> fields = new LinkedList<>(getFlatFields());
    fields.add(
        subsectionWithPath("guesses")
            .description("All guesses submitted for this code, in order of submission.")
            .type(JsonFieldType.ARRAY)
    );
    return fields;
  }

}
