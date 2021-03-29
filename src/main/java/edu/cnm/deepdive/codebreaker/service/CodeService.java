package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.controller.CodebreakerExceptionHandler.InvalidPropertyException;
import edu.cnm.deepdive.codebreaker.model.dao.CodeRepository;
import edu.cnm.deepdive.codebreaker.model.entity.Code;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class CodeService {

  private static final String POOL_PROPERTY = "pool";
  private static final String NULL_CHARACTER_MESSAGE = "must not contain null character";

  private final CodeRepository codeRepository;
  private final UUIDStringifier stringifier;
  private final Random rng;

  @Autowired
  public CodeService(CodeRepository codeRepository, UUIDStringifier stringifier, Random rng) {
    this.codeRepository = codeRepository;
    this.stringifier = stringifier;
    this.rng = rng;
  }

  public Code add(@NonNull Code code) {
    int[] pool = code
        .getPool()
        .codePoints()
        .distinct()
        .toArray();
    if (Arrays.stream(pool).anyMatch((v) -> v == 0)) {
      throw new InvalidPropertyException(POOL_PROPERTY, NULL_CHARACTER_MESSAGE);
    }
    code.setPool(new String(pool, 0, pool.length));
    int[] secret = IntStream
        .generate(() -> pool[rng.nextInt(pool.length)])
        .limit(code.getLength())
        .toArray();
    String text = new String(secret, 0, secret.length);
    code.setText(text);
    return codeRepository.save(code);
  }

  public Optional<Code> get(@NonNull String id) {
    UUID uuid = stringifier.fromString(id);
    return codeRepository.findById(uuid);
  }

  public void remove(@NonNull Code code) {
    codeRepository.delete(code);
  }

  public Iterable<Code> getAll() {
    return codeRepository.getAllByOrderByCreatedDesc();
  }

  public void clear() {
    codeRepository.deleteAll();
  }

}
