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
package edu.cnm.deepdive.codebreaker.model.dao;

import edu.cnm.deepdive.codebreaker.model.entity.Game;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Extends the {@link JpaRepository} interface for the {@link Game} entity. In addition to the
 * data-access operations declared in {@code JpaRepository}, this interface declares queries to
 * retrieve {@link Iterable Iterable&lt;Code&gt;}, optionally filtering on solution state, and
 * in descending order by creation date (i.e. most recent first).
 */
public interface GameRepository extends JpaRepository<Game, Long> {

  /**
   * Queries and returns the {@link Game} instance (if it exists) with the specified external
   * identifier.
   *
   * @param externalKey Resource identifier.
   * @return Specified {@link Game} instance, if it exists.
   */
  Optional<Game> findByExternalKey(UUID externalKey);

  /**
   * Queries and returns all {@link Game} instances that have no guesses recorded since the {@code
   * cutoff} date.
   * @param cutoff Threshold date for most recently recorded guess in a stale game.
   * @return Stale (not recently modified) {@link Game} instances.
   */
  @Query("SELECT c FROM Game AS c WHERE c.created < :cutoff AND NOT EXISTS (SELECT g FROM Guess AS g WHERE g.game = c AND g.created > :cutoff)")
  Iterable<Game> findAllStale(Date cutoff);

}
