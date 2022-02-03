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
package edu.cnm.deepdive.codebreaker.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.cnm.deepdive.codebreaker.configuration.Beans;
import edu.cnm.deepdive.codebreaker.view.UUIDSerializer;
import edu.cnm.deepdive.codebreaker.view.UUIDStringifier;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.lang.NonNull;

/**
 * Encapsulates a single guess, submitted by a codebreaker, against a {@link Game}. Annotations are
 * used to specify the view&mdash;the JSON representation of the guess.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(
    indexes = @Index(columnList = "created")
)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "created", "text", "exactMatches", "nearMatches", "solution", "href"})
public class Guess {

  private static AtomicReference<EntityLinks> entityLinks = new AtomicReference<>();
  private static AtomicReference<UUIDStringifier> stringifier = new AtomicReference<>();

  @NonNull
  @Id
  @GeneratedValue
  @Column(name = "guess_id", updatable = false, columnDefinition = "UUID")
  @JsonIgnore
  private UUID id;

  @NonNull
  @Column(nullable = false, updatable = false, unique = true, columnDefinition = "UUID")
  @JsonProperty(value = "id", access = Access.READ_ONLY)
  @JsonSerialize(converter = UUIDSerializer.class)
  private UUID externalKey;

  @NonNull
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  @JsonProperty(access = Access.READ_ONLY)
  private Date created;

  @NonNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "game_id", nullable = false, updatable = false)
  @JsonIgnore
  private Game game;

  @NonNull
  @Column(length = Game.MAX_CODE_LENGTH, name = "guess_text", nullable = false, updatable = false)
  @NotEmpty
  @Size(max = Game.MAX_CODE_LENGTH)
  private String text;

  @Column(nullable = false, updatable = false)
  @JsonProperty(access = Access.READ_ONLY)
  private int exactMatches;

  @Column(nullable = false, updatable = false)
  @JsonProperty(access = Access.READ_ONLY)
  private int nearMatches;

  @Transient
  @JsonProperty(access = Access.READ_ONLY)
  private URI href;

  /**
   * Returns the primary key and (internal) unique identifier of this guess.
   */
  @NonNull
  public UUID getId() {
    return id;
  }

  /**
   * Returns the external identifier of this guess.
   */
  @NonNull
  public UUID getExternalKey() {
    return externalKey;
  }

  /**
   * Returns the date this guess was first submitted and persisted to the database.
   */
  @NonNull
  public Date getCreated() {
    return created;
  }

  /**
   * Returns the {@link Game} instance against which this guess was submitted.
   */
  @NonNull
  public Game getGame() {
    return game;
  }

  /**
   * Sets the {@link Game} instance against which this guess was submitted.
   */
  public void setGame(@NonNull Game game) {
    this.game = game;
  }

  /**
   * Returns the text of this guess.
   */
  @NonNull
  public String getText() {
    return text;
  }

  /**
   * Sets the text of this guess.
   */
  public void setText(@NonNull String text) {
    this.text = text;
  }

  /**
   * Returns the number of characters in this guess which are found in the same positions in the
   * code.
   */
  public int getExactMatches() {
    return exactMatches;
  }

  /**
   * Sets the number of characters in this guess which are found in the same positions in the code.
   */
  public void setExactMatches(int exactMatches) {
    this.exactMatches = exactMatches;
  }

  /**
   * Returns the number of characters in this guess which are found in different positions in the
   * code (not counting those characters in the code that are matched exactly by other occurrences
   * of the same character in the guess).
   */
  public int getNearMatches() {
    return nearMatches;
  }

  /**
   * Sets the number of characters in this guess which are found in different positions in the code
   * (not counting those characters in the code that are matched exactly by other occurrences of the
   * same character in the guess).
   */
  public void setNearMatches(int nearMatches) {
    this.nearMatches = nearMatches;
  }

  /**
   * Returns the {@link URI} that can be used to reference this instance via a HTTP GET request.
   */
  public URI getHref() {
    return href;
  }

  /**
   * Returns a {@code boolean} flag indicating whether this guess matches the code exactly.
   */
  public boolean isSolution() {
    return exactMatches == game.getLength();
  }

  @PrePersist
  private void generateExternalKey() {
    externalKey = UUID.randomUUID();
  }

  @PostLoad
  @PostPersist
  private void updateTransients() {
    entityLinks.compareAndSet(null, Beans.bean(EntityLinks.class));
    stringifier.compareAndSet(null, Beans.bean(UUIDStringifier.class));
    UUIDStringifier stringifier = Guess.stringifier.get();
    href = entityLinks
        .get()
        .linkFor(Guess.class, stringifier.toString(game.getExternalKey()))
        .slash(stringifier.toString(externalKey))
        .toUri();
  }

}
