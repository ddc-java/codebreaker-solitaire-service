package edu.cnm.deepdive.codebreaker.model.dao;

import edu.cnm.deepdive.codebreaker.model.entity.Code;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CodeRepository extends JpaRepository<Code, UUID> {

  Iterable<Code> getAllByOrderByCreatedDesc();

  @Query("SELECT c FROM Guess AS g JOIN g.code c WHERE c.length = g.exactMatches ORDER BY c.created DESC")
  Iterable<Code> getAllSolvedOrderByCreatedDesc();

  @Query("SELECT c FROM Code AS c WHERE NOT EXISTS (SELECT g FROM Guess AS g WHERE g.code = c AND g.exactMatches = c.length) ORDER BY c.created DESC")
  Iterable<Code> getAllUnsolvedOrderByCreatedDesc();

}
