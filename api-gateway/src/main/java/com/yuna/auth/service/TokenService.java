package com.yuna.auth.service;

import com.yuna.auth.model.User;
import com.yuna.auth.model.Token;

public interface TokenService {

  public Boolean saveToken(String token);

  public Boolean saveToken(Token token);

  public Boolean invalidateToken(String token);

  public Token getToken(String token);

  public String generateToken(User user);

  public Boolean isTokenValid(Token token);

  public Boolean isTokenValid(String token);
}
