package edu.cnm.deepdive.codebreaker.controller;

public class ValidationPatterns {

  public static final String ID_PATTERN = "[\\da-zA-Z]{26}";

  public static final String ID_PATH_PARAMETER_PATTERN = "/{id:" + ID_PATTERN +  "}";

}
