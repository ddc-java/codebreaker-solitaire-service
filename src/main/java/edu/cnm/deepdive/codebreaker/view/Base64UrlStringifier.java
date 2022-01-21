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
package edu.cnm.deepdive.codebreaker.view;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Service implementing the {@link UUIDStringifier} interface for a base64url representation of the
 * unsigned 128-bit value encapsulated in an instance of {@link UUID}.
 */
@Component
public class Base64UrlStringifier implements UUIDStringifier {

  private final Encoder encoder = Base64.getUrlEncoder().withoutPadding();
  private final Decoder decoder = Base64.getUrlDecoder();

  /**
   * Constructs and returns a base64url representation of the 128 bits in the {@link UUID} {@code
   * value}. This is intended not to provide any kind of encryption or other security (it doesn't),
   * but simply to provide a less clunky (and slightly more opaque) form of a UUID, for use in
   * URLs.
   *
   * @param value {@link UUID} for which a {@link String} representation is to be constructed.
   * @return base64url representation of {@code value}, or {@code null} if {@code value} is {@code
   * null}.
   */
  @Override
  public String toString(UUID value) {
    return (value != null) ? encoder.encodeToString(asBytes(value)) : null;
  }

  /**
   * Converts a {@link String} {@code value}, assumed to be a base64url representation of a {@code
   * byte[]} of length 16, to an instance of {@link UUID}.
   *
   * @param value {@link String} value to be converted to a {@link UUID}.
   * @return Constructed {@link UUID} corresponding to {@code value}, or {@code null} if {@code
   * value} is {@code null}.
   * @throws IllegalArgumentException If {@code value} is not properly .
   */
  @Override
  public UUID fromString(String value) throws IllegalArgumentException {
    return asUUID(decoder.decode(value));
  }

  private byte[] asBytes(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  private UUID asUUID(byte[] bytes) {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    long firstLong = bb.getLong();
    long secondLong = bb.getLong();
    return new UUID(firstLong, secondLong);
  }

}
