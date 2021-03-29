package edu.cnm.deepdive.codebreaker;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;

import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class CommonFieldDescriptors {

  public static List<FieldDescriptor> getExceptionFields() {
    return List.of(
        fieldWithPath("timestamp")
            .description("Timestamp of request.")
            .type(JsonFieldType.STRING),
        fieldWithPath("status")
            .description("HTTP response status code.")
            .type(JsonFieldType.NUMBER),
        fieldWithPath("error")
            .description("HTTP response status text.")
            .type(JsonFieldType.STRING),
        fieldWithPath("message")
            .description("Additional error message.")
            .type(JsonFieldType.STRING),
        fieldWithPath("path")
            .description("Host-relative URL of request.")
            .type(JsonFieldType.STRING),
        subsectionWithPath("details")
            .description("Additional details, generally included with validation errors.")
            .optional()
    );
  }
}
