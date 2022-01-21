/*
 *  Copyright 2022 CNM Ingenuity, Inc.
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
            .type(JsonFieldType.OBJECT)
            .optional()
    );
  }

}
