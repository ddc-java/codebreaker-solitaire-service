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

import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Extends the {@link JpaRepository} interface for the {@link Code} entity. In addition to the
 * data-access operations declared in {@code JpaRepository}, this interface declares queries to
 * retrieve {@link Iterable Iterable&lt;Code&gt;}, optionally filtering on solution state, and
 * in descending order by creation date (i.e. most recent first).
 */
public interface CodeRepository extends JpaRepository<Code, UUID> {

  /**
   * Queries and returns all {@link Code} instances, in descending order by creation date.
   * @return
   */
  Stream<Code> getAllByOrderByCreatedDesc();

  /**
   * Queries and returns all solved {@link Code} instances, in descending order by creation date.
   * @return
   */
  @Query("SELECT DISTINCT c FROM Guess AS g JOIN g.code c WHERE c.length = g.exactMatches ORDER BY c.created DESC")
  Stream<Code> getAllSolvedOrderByCreatedDesc();

  /**
   * Queries and returns all unsolved {@link Code} instances, in descending order by creation date.
   * @return
   */
  @Query("SELECT c FROM Code AS c WHERE NOT EXISTS (SELECT g FROM Guess AS g WHERE g.code = c AND g.exactMatches = c.length) ORDER BY c.created DESC")
  Stream<Code> getAllUnsolvedOrderByCreatedDesc();

}
