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
package edu.cnm.deepdive.codebreaker.service;

import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Service implementing the {@link UUIDStringifier} interface for a base-36 representation (using
 * the characters '0'&hellip;'9' and 'a'&hellip;'f') of the unsigned 128-bit value encapsulated in
 * an instance of {@link UUID}.
 */
@Service
public class Base36Stringifier implements UUIDStringifier {

  private static final int RADIX = 36;
  private static final int PADDED_LONG_REPRESENTATION_SIZE = 13;
  private static final String PADDED_LONG_FORMAT =
      String.format("%%%ds", PADDED_LONG_REPRESENTATION_SIZE);
  private static final String PARSE_FAILURE_FORMAT = "Specified key cannot be parsed: \"%s\".";

  /**
   * Constructs and returns a lowercase base-36 representation of the 128 bits in the {@link UUID}
   * {@code value}. This is intended to be used as an opaque key; however, for debugging or other
   * informational purposes, it may be assumed that the rightmost 13 characters of the
   * representation returned from this method correspond to the least significant 64 bits of the
   * {@code UUID}, and the characters to the left of that correspond to the most significant bits.
   *
   * @param value {@link UUID} for which a {@link String} representation is to be constructed.
   * @return Base-36 representation of {@code value}, or {@code null} if {@code value} is {@code
   * null}.
   */
  @Override
  public String toString(UUID value) {
    return (value != null)
        ? (longToString(value.getMostSignificantBits(), false)
        + longToString(value.getLeastSignificantBits(), true))
        : null;
  }

  /**
   * Parses a {@link String} {@code value}, assumed to be a base-36 representation
   * (case-insensitive) of a 128-bit unsigned integer, to an instance of {@link UUID}.
   *
   * @param value {@link String} value to be converted to a {@link UUID}.
   * @return Constructed {@link UUID} corresponding to {@code value}, or {@code null} if {@code
   * value} is {@code null}.
   * @throws IllegalArgumentException If {@code value} cannot be parsed as a base-36 unsigned
   *                                  integer, if it is of insufficient length to contain the 128
   *                                  bits of a UUID.
   */
  @Override
  public UUID fromString(String value) throws IllegalArgumentException {
    try {
      UUID id;
      if (value != null) {
        int lsbStart = value.length() - PADDED_LONG_REPRESENTATION_SIZE;
        id = new UUID(stringToLong(value.substring(0, lsbStart)),
            stringToLong(value.substring(lsbStart)));
      } else {
        id = null;
      }
      return id;
    } catch (IndexOutOfBoundsException | NumberFormatException e) {
      throw new IllegalArgumentException(String.format(PARSE_FAILURE_FORMAT, value), e);
    }
  }

  private long stringToLong(@NonNull String value) throws NumberFormatException {
    return Long.parseUnsignedLong(value, RADIX);
  }

  private String longToString(long value, boolean zeroPadded) {
    String baseRepresentation = Long.toUnsignedString(value, RADIX);
    return zeroPadded
        ? String.format(PADDED_LONG_FORMAT, baseRepresentation).replace(' ', '0')
        : baseRepresentation;
  }

}
