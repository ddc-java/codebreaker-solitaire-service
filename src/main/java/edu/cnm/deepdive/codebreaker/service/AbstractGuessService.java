package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.NonNull;

public interface AbstractGuessService {

  /**
   * Validates and matches the specified {@link Guess} against the related {@link Game}, and adds
   * the former to the latter's collection of guesses. {@link Guess#getText() guess.getText()}{@link
   * String#length() .length()} must be equal to {@link Game#getLength() game.getLength()}, and
   * {@link Guess#getText() guess.getText()} must not include any characters that are not present in
   * {@link Game#getPool() game.getPool()}. If these conditions are met, then {@link
   * Guess#getText()} and {@link Game#getText()} are compared, to compute the number of exact and
   * near-matches. Finally, {@code guess} is saved to the collection, as one of {@code game}'s
   * guesses.
   *
   * @param game  Game with secret code being guessed.
   * @param guess Submitted attempt to guess the secret code.
   * @return Validated, summarized (in the numbers of matches), and saved {@code Guess}.
   */
  Guess add(@NonNull Game game, @NonNull Guess guess) throws InvalidPropertyException;

  /**
   * Retrieves an {@link Optional Optional&lt;Guess&gt;}, specified by {@code externalKey}, from the
   * collection. If there is no instance with the specified {@code externalKey} in the collection,
   * the {@link Optional} returned is empty.
   *
   * @param externalKey Unique identifier of {@link Guess} instance.
   * @return {@link Optional Optional&lt;Guess&gt;} containing {@link Guess} referenced by {@code
   * externalKey} (if it exists).
   */
  Optional<Guess> get(@NonNull Game game, @NonNull UUID externalKey);
}
