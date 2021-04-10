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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.NonNull;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(
    indexes = @Index(columnList = "created")
)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "created", "text", "exactMatches", "nearMatches", "solution", "code"})
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
  private Date created;

  @NonNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "code_id", nullable = false, updatable = false)
  @JsonIgnore
  private Code code;

  @NonNull
  @Column(name = "guess_text", nullable = false, updatable = false)
  @NotEmpty
  private String text;

  public int exactMatches;

  public int nearMatches;

  @Transient
  @JsonProperty(value = "id", access = Access.READ_ONLY)
  private String key;

  @NonNull
  public UUID getId() {
    return id;
  }

  @NonNull
  public Date getCreated() {
    return created;
  }

  @NonNull
  public Code getCode() {
    return code;
  }

  public void setCode(@NonNull Code code) {
    this.code = code;
  }

  @NonNull
  public String getText() {
    return text;
  }

  public void setText(@NonNull String text) {
    this.text = text;
  }

  public int getExactMatches() {
    return exactMatches;
  }

  public void setExactMatches(int exactMatches) {
    this.exactMatches = exactMatches;
  }

  public int getNearMatches() {
    return nearMatches;
  }

  public void setNearMatches(int nearMatches) {
    this.nearMatches = nearMatches;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public boolean isSolution() {
    return exactMatches == code.getLength();
  }

  @JsonIgnore
  public int[] codePoints() {
    return text
        .codePoints()
        .toArray();
  }

  @PostLoad
  @PostPersist
  private void updateKey() {
    UUIDStringifier stringifier = Beans.bean(UUIDStringifier.class);
    key = stringifier.toString(id);
  }

}
