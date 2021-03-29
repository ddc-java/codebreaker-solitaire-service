package edu.cnm.deepdive.codebreaker.model.dao;

import edu.cnm.deepdive.codebreaker.model.entity.Code;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeRepository extends JpaRepository<Code, UUID> {

  Iterable<Code> getAllByOrderByCreatedDesc();

}
