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
package edu.cnm.deepdive.codebreaker.configuration;

import java.security.SecureRandom;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Utility class providing {@link Bean}-annotated methods to satisfy dependencies (declared in
 * Spring components) on instances of classes not declared as Spring beans.
 */
@Configuration
public class Beans {

  /**
   * Returns an instance of {@link Random} (or a subclass), which may be used by Spring to satisfy
   * an explicit bean request or a dependency declared by a Spring component.
   *
   * @return (See above.)
   */
  @SuppressWarnings("JavaDoc")
  @Bean
  public Random getRandom() {
    return new SecureRandom();
  }

}
