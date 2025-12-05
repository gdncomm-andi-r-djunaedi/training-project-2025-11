package com.yuna.auth.repository;

import com.yuna.auth.model.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TokenRepository extends MongoRepository<Token, String> {

  public Token findByToken (String token);
}
