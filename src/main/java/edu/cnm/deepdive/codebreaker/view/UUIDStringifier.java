/*
 *  Copyright 2021 CNM Ingenuity, Inc.
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

import java.util.UUID;

/**
 * Interface declaring methods for 2-way conversion between {@link UUID} instances and {@link
 * String} representations, facilitating the use of these {@code String} representations as
 * consumer-facing keys. Ideally, an implementation of this interface should construct {@code
 * String} representations that are shorter and (at least slightly) more opaque than those returned
 * by {@link UUID#toString()}.
 * <p><strong>Important</strong>: Note that opacity of a key&mdash;whatever the representational
 * strategy used&mdash;is not a substitute for proper security on service endpoints. In other words,
 * "security through obscurity" is <strong>not</strong> an acceptable approach.</p>
 */
public interface UUIDStringifier {

  /**
   * Constructs and returns a {@link String} representation of the provided {@link UUID} {@code
   * value}. This interface does not specify how a {@code null} {@code value} should be handled; the
   * approach followed by a given implementation should be documented explicitly.
   *
   * @param value {@link UUID} for which a {@link String} representation is to be constructed.
   * @return Constructed {@link String} representation.
   */
  String toString(UUID value);

  /**
   * Constructs and returns a {@link UUID} instance from the provided {@link String} {@code value}.
   * This interface does not specify how a {@code null} {@code value} should be handled; the
   * approach followed by a given implementation should be documented explicitly.
   *
   * @param value {@link String} value to be converted to a {@link UUID}.
   * @return Constructed {@link UUID} corresponding to {@code value}.
   * @throws IllegalArgumentException If {@code value} cannot be translated into a {@link UUID},
   *                                  according to the requirements of a given implementation.
   */
  UUID fromString(String value) throws IllegalArgumentException;

}
