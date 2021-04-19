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
package edu.cnm.deepdive.codebreaker.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import edu.cnm.deepdive.codebreaker.configuration.Beans;
import edu.cnm.deepdive.codebreaker.service.UUIDStringifier;
import java.util.Date;
import java.util.UUID;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.NonNull;

/**
 * Encapsulates a single guess, submitted by a codebreaker, against a {@link Code}. Annotations are
 * used to specify the view&mdash;the JSON representation of the guess.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(
    indexes = @Index(columnList = "created")
)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "created", "text", "exactMatches", "nearMatches", "solution"})
public class Guess {

  @NonNull
  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Column(name = "guess_id", updatable = false, columnDefinition = "CHAR(16) FOR BIT DATA")
  @JsonIgnore
  private UUID id;

  @NonNull
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  @JsonProperty(access = Access.READ_ONLY)
  private Date created;

  @NonNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "code_id", nullable = false, updatable = false)
  @JsonIgnore
  private Code code;

  @NonNull
  @Column(length = Code.MAX_CODE_LENGTH, name = "guess_text", nullable = false, updatable = false)
  @NotEmpty
  @Size(max = Code.MAX_CODE_LENGTH)
  private String text;

  @Column(nullable = false, updatable = false)
  @JsonProperty(access = Access.READ_ONLY)
  private int exactMatches;

  @Column(nullable = false, updatable = false)
  @JsonProperty(access = Access.READ_ONLY)
  private int nearMatches;

  @Transient
  @JsonProperty(value = "id", access = Access.READ_ONLY)
  private String key;

  /**
   * Returns the unique identifier of this guess.
   *
   * @return
   */
  @NonNull
  public UUID getId() {
    return id;
  }

  /**
   * Returns the date this guess was first submitted and persisted to the database.
   *
   * @return
   */
  @NonNull
  public Date getCreated() {
    return created;
  }

  /**
   * Returns the {@link Code} instance against which this guess was submitted.
   *
   * @return
   */
  @NonNull
  public Code getCode() {
    return code;
  }

  /**
   * Sets the {@link Code} instance against which this guess was submitted.
   *
   * @param code
   */
  public void setCode(@NonNull Code code) {
    this.code = code;
  }

  /**
   * Returns the text of this guess.
   *
   * @return
   */
  @NonNull
  public String getText() {
    return text;
  }

  /**
   * Sets the text of this guess.
   *
   * @param text
   */
  public void setText(@NonNull String text) {
    this.text = text;
  }

  /**
   * Returns the number of characters in this guess which are found in the same positions in the
   * code.
   *
   * @return
   */
  public int getExactMatches() {
    return exactMatches;
  }

  /**
   * Sets the number of characters in this guess which are found in the same positions in the code.
   *
   * @param exactMatches
   */
  public void setExactMatches(int exactMatches) {
    this.exactMatches = exactMatches;
  }

  /**
   * Returns the number of characters in this guess which are found in different positions in the
   * code (not counting those characters in the code that are matched exactly by other occurrences
   * of the same character in the guess).
   *
   * @return
   */
  public int getNearMatches() {
    return nearMatches;
  }

  /**
   * Sets the number of characters in this guess which are found in different positions in the code
   * (not counting those characters in the code that are matched exactly by other occurrences of the
   * same character in the guess).
   *
   * @param nearMatches
   */
  public void setNearMatches(int nearMatches) {
    this.nearMatches = nearMatches;
  }

  /**
   * Returns a {@link String}-valued representation of the unique identifier of this guess.
   *
   * @return
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the (transient) {@link String}-valued representation of the unique identifier of this
   * guess.
   *
   * @param key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Returns a {@code boolean} flag indicating whether this guess matches the code exactly.
   *
   * @return
   */
  public boolean isSolution() {
    return exactMatches == code.getLength();
  }

  @PostLoad
  @PostPersist
  private void updateKey() {
    UUIDStringifier stringifier = Beans.bean(UUIDStringifier.class);
    key = stringifier.toString(id);
  }

}
