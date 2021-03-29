package edu.cnm.deepdive.codebreaker.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonView;
import edu.cnm.deepdive.codebreaker.view.CodeView;
import edu.cnm.deepdive.codebreaker.view.GuessView;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
@JsonView({GuessView.Flat.class, CodeView.Hierarchical.class})
public class Guess {

  @NonNull
  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Column(name = "guess_id", updatable = false, columnDefinition = "CHAR(16) FOR BIT DATA")
  @JsonProperty(access = Access.READ_ONLY)
  private UUID id;

  @NonNull
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  private Date created;

  @NonNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "code_id", nullable = false, updatable = false)
  @JsonView(GuessView.Hierarchical.class)
  private Code code;

  @NonNull
  @Column(name = "guess_text", nullable = false, updatable = false)
  @NotEmpty
  private String text;

  public int exactMatches;

  public int nearMatches;

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

  public boolean isSolution() {
    return exactMatches == code.getLength();
  }

  @JsonIgnore
  public int[] codePoints() {
    return text
        .codePoints()
        .toArray();
  }

}
