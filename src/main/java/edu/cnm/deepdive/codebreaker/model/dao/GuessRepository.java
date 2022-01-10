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
package edu.cnm.deepdive.codebreaker.model.dao;

import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Extends the {@link JpaRepository} interface for the {@link Guess} entity. In the current
 * implementation, no additional data-access operations are declared, beyond those in {@code
 * JpaRepository}.
 */
public interface GuessRepository extends JpaRepository<Guess, UUID> {


  /**
   * Queries and returns the {@link Guess} instance (if it exists) with the specified external
   * identifier.
   *
   * @param externalId Resource identifier.
   * @return Specified {@link Guess} instance, if it exists.
   */
  Optional<Guess> findByExternalId(UUID externalId);

}
