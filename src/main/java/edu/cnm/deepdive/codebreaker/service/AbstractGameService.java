package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.NonNull;

public interface AbstractGameService {

  /**
   * Validates, completes, and adds the partially-specified {@link Game} instance to the collection.
   * Minimally, {@code game} must contain a character pool and game length; if the text of the game
   * is not included (which is always the case for {@link Game} instances deserialized from JSON),
   * random text is automatically generated.
   *
   * @param game Partial {@link Game}.
   * @return Completed and persisted {@link Game} instance.
   * @throws InvalidPropertyException If {@code game} contains any invalid characters (whitespace,
   *                                  control characters, or characters not included in the UCD).
   */
  Game add(@NonNull Game game) throws InvalidPropertyException;

  /**
   * Retrieves an {@link Optional Optional&lt;Game&gt;}, specified by {@code externalKey}, from the
   * collection. If there is no instance with the specified {@code externalKey} in the collection,
   * the {@link Optional} returned is empty.
   *
   * @param externalKey Unique identifier of {@link Game} instance.
   * @return {@link Optional Optional&lt;Game&gt;} containing {@link Game} referenced by {@code
   * externalKey} (if it exists).
   */
  Optional<Game> get(@NonNull UUID externalKey);

  /**
   * Removes the specified {@link Game} instance from the collection.
   *
   * @param game {@link Game} to be removed.
   */
  void remove(@NonNull Game game);

  /**
   * Removes all games from the collection.
   */
  void clear();
}
