package edu.cnm.deepdive.codebreaker.model.dao;

import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuessRepository extends JpaRepository<Guess, UUID> {

}
