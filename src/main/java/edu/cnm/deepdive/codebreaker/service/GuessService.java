package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.AlreadySolvedException;
import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.dao.CodeRepository;
import edu.cnm.deepdive.codebreaker.model.dao.GuessRepository;
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class GuessService {

  private static final String INVALID_CHARACTERS = "^.*[^%s].*$";
  private static final String TEXT_PROPERTY = "text";
  private static final String INVALID_CHARACTER_FORMAT =
      "must contain no characters other than \"%s\"";
  private static final String INVALID_LENGTH_FORMAT = "must have a length of exactly %d characters";

  private final GuessRepository guessRepository;

  @Autowired
  public GuessService(GuessRepository guessRepository) {
    this.guessRepository = guessRepository;
  }

  public Guess add(@NonNull Code code, @NonNull Guess guess) {
    validate(code, guess);
    int numCorrect = 0;
    int numClose = 0;
    int[] codeCodePoints = code.codePoints();
    int[] guessCodePoints = guess.codePoints();
    for (int i = 0; i < guessCodePoints.length; i++) {
      if (guessCodePoints[i] == codeCodePoints[i]) {
        numCorrect++;
        codeCodePoints[i] = guessCodePoints[i] = 0;
      }
    }
    for (int codePoint : guessCodePoints) {
      if (codePoint != 0) {
        for (int j = 0; j < codeCodePoints.length; j++) {
          if (codePoint == codeCodePoints[j]) {
            numClose++;
            codeCodePoints[j] = 0;
            break;
          }
        }
      }
    }
    guess.setExactMatches(numCorrect);
    guess.setNearMatches(numClose);
    guess.setCode(code);
    return guessRepository.save(guess);
  }

  public Optional<Guess> get(@NonNull UUID id) {
    return guessRepository.findById(id);
  }

  private void validate(Code code, Guess guess) {
    if (code.isSolved()) {
      throw new AlreadySolvedException();
    }
    String guessText = guess.getText();
    String poolText = code.getPool();
    if (guessText.matches(String.format(INVALID_CHARACTERS, code.getPool()))) {
      throw new InvalidPropertyException(
          TEXT_PROPERTY, String.format(INVALID_CHARACTER_FORMAT, poolText));
    }
    if (guess.codePoints().length != code.getLength()) {
      throw new InvalidPropertyException(
          TEXT_PROPERTY, String.format(INVALID_LENGTH_FORMAT, code.getLength()));
    }
  }

}
