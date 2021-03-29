package edu.cnm.deepdive.codebreaker.service;

import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class Base36Stringifier implements
    UUIDStringifier {

  @Override
  public String toString(UUID value) {
    return (value != null)
        ? (longToString(value.getMostSignificantBits())
            + longToString(value.getLeastSignificantBits()))
        : null;
  }

  @Override
  public UUID fromString(String value) throws NumberFormatException {
    return (value != null)
        ? new UUID(stringToLong(value.substring(0, 13)), stringToLong(value.substring(13)))
        : null;
  }

  private long stringToLong(@NonNull String value) throws NumberFormatException{
    return Long.parseUnsignedLong(value, 36);
  }

  private String longToString(long value) {
    return String.format("%13s", Long.toUnsignedString(value, 36)).replace(' ', '0');
  }

}
