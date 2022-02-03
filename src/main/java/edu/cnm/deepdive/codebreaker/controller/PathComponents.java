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

class PathComponents {

  static final String ID_PATTERN = "[-\\w]{22}";
  static final String GAMES_COMPONENT = "/games";
  static final String GAMES_PATH = GAMES_COMPONENT;
  static final String GAME_ID_COMPONENT = "/{gameId:" + ID_PATTERN +  "}";
  static final String GUESSES_COMPONENT = "/guesses";
  static final String GUESSES_PATH = GAMES_COMPONENT + GAME_ID_COMPONENT + GUESSES_COMPONENT;
  static final String GUESS_ID_COMPONENT = "/{guessId:" + ID_PATTERN +  "}";

}
