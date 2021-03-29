package edu.cnm.deepdive.codebreaker.controller;

public class ValidationPatterns {

  public static final String UUID_PATTERN =
      "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";

  public static final String UUID_PATH_PARAMETER_PATTERN = "/{id:" + UUID_PATTERN +  "}";

}
