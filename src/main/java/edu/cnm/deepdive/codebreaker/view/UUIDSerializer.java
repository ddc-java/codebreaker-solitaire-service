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
package edu.cnm.deepdive.codebreaker.view;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Provides a serializer, injected automatically into controller methods and referenced in
 * HATEOAS-related code in DTO classes, able to construct a (presumably) user-friendly {@link
 * String} representation of a {@link UUID}.
 */
@Component
public class UUIDSerializer extends StdConverter<UUID, String> implements Converter<UUID, String> {

  private final UUIDStringifier stringifier;

  /**
   * Initializes this instance with an autowired {@link UUIDStringifier} implementation.
   *
   * @param stringifier {@link UUIDStringifier} instance to be used in serialization.
   */
  @Autowired
  public UUIDSerializer(UUIDStringifier stringifier) {
    this.stringifier = stringifier;
  }

  @Override
  public String convert(UUID id) {
    return stringifier.toString(id);
  }

}
