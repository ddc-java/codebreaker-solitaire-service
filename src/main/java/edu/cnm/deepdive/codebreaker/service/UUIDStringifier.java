package edu.cnm.deepdive.codebreaker.service;

import java.util.UUID;

public interface UUIDStringifier {

  String toString(UUID value);

  UUID fromString(String value) throws NumberFormatException;

}
