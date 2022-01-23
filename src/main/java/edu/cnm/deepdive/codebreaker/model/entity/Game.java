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
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.cnm.deepdive.codebreaker.configuration.Beans;
import edu.cnm.deepdive.codebreaker.view.GameProjection;
import edu.cnm.deepdive.codebreaker.view.UUIDSerializer;
import edu.cnm.deepdive.codebreaker.view.UUIDStringifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.lang.NonNull;

/**
 * Encapsulates the persistent and transient attributes of a single code created (game started) by a
 * codemaker. Annotations are used to specify the view&mdash;the JSON representation of the
 * code&mdash;which changes, depending on whether the code has been solved (guessed) successfully.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(
    indexes = @Index(columnList = "created")
)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "created", "pool", "length", "guessCount", "solved", "text", "href"})
@JsonView(GameProjection.Simple.class)
public class Game {

  /**
   * Maximum allowed length of a generated code (and any guess submitted against the code).
   */
  public static final int MAX_CODE_LENGTH = 20;
  private static final int MAX_POOL_LENGTH = 255;

  private static AtomicReference<EntityLinks> entityLinks = new AtomicReference<>();
  private static AtomicReference<UUIDStringifier> stringifier = new AtomicReference<>();

  @NonNull
  @Id
  @GeneratedValue
  @Column(name = "game_id", updatable = false, columnDefinition = "UUID")
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
  @Column(length = MAX_POOL_LENGTH, nullable = false, updatable = false)
  @NotEmpty
  @Size(max = MAX_POOL_LENGTH)
  private String pool;

  @Column(name = "code_text", length = MAX_CODE_LENGTH, nullable = false, updatable = false)
  @JsonIgnore
  private String text;

  @Column(nullable = false, updatable = false)
  @Min(1)
  @Max(MAX_CODE_LENGTH)
  private int length;

  @NonNull
  @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("created ASC")
  @JsonView(GameProjection.Detailed.class)
  private final List<Guess> guesses = new ArrayList<>();

  @Transient
  @JsonProperty(access = Access.READ_ONLY)
  private URI href;

  /**
   * Returns the primary key and (internal) unique identifier of this code.
   */
  @NonNull
  public UUID getId() {
    return id;
  }

  /**
   * Returns the external identifier of this code.
   */
  @NonNull
  public UUID getExternalKey() {
    return externalKey;
  }

  /**
   * Returns the date this code was first created and persisted to the database.
   */
  @NonNull
  public Date getCreated() {
    return created;
  }

  /**
   * Returns (as a {@code String}) the pool of characters from which the code of this game was
   * generated.
   */
  @NonNull
  public String getPool() {
    return pool;
  }

  /**
   * Sets the pool of characters from which the code of this game was generated. This pool is not
   * used after generation, but is intended to be returned to the client for informational purposes
   * only.
   */
  public void setPool(@NonNull String pool) {
    this.pool = pool;
  }

  /**
   * Returns the generated code. This is not intended to be returned to the client; instead, the
   * {@link #getSolution()} method should be used for state-dependent return of this value.
   */
  public String getText() {
    return text;
  }

  /**
   * Sets the generated code to be guessed.
   */
  public void setText(@NonNull String code) {
    this.text = code;
  }

  /**
   * Returns the length of the code. This pool is not used after generation, but is intended to be
   * returned to the client for informational purposes only.
   */
  public int getLength() {
    return length;
  }

  /**
   * Sets the length of the code to be guessed.
   */
  public void setLength(int length) {
    this.length = length;
  }

  /**
   * Returns the {@link List List&lt;Guess&gt;} of guesses submitted against this code.
   */
  @NonNull
  public List<Guess> getGuesses() {
    return guesses;
  }

  /**
   * Returns the {@link URI} that can be used to reference this instance via a HTTP GET request.
   */
  public URI getHref() {
    return href;
  }

  /**
   * Returns a {@code boolean} flag indicating whether the code has been guessed successfully.
   */
  public boolean isSolved() {
    return guesses
        .stream()
        .anyMatch(Guess::isSolution);
  }

  /**
   * Returns the generated code, if it has been guessed successfully; otherwise, {@code null} is
   * returned.
   */
  @JsonProperty("text")
  public String getSolution() {
    return isSolved() ? text : null;
  }

  /**
   * Returns the count of guesses submitted in this game.
   */
  public int getGuessCount() {
    return guesses.size();
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
    href = entityLinks
        .get()
        .linkForItemResource(Game.class, stringifier.get().toString(externalKey))
        .toUri();
  }

}