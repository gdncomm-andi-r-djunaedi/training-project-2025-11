package com.yuna.auth.service.impl;

import com.yuna.auth.model.User;
import com.yuna.auth.model.Token;
import com.yuna.auth.repository.TokenRepository;
import com.yuna.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenServiceImpl implements TokenService {

  @Autowired
  private TokenRepository tokenRepository;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpiration;

  @Autowired
  private JwtService jwtService;

  @Override
  public Boolean saveToken(String token) {
    Token tokenModel = Token.builder().token(token).expiration(new Date(System.currentTimeMillis() + jwtExpiration)).build();
    tokenRepository.save(tokenModel);
    return true;
  }

  @Override
  public Boolean saveToken(Token token) {
    tokenRepository.save(token);
    return true;
  }

  @Override
  public Boolean invalidateToken(String token) {
    Token tokenModel = tokenRepository.findByToken(token);
    tokenRepository.delete(tokenModel);
    return true;
  }

  @Override
  public Token getToken(String token) {
    return tokenRepository.findByToken(token);
  }

  @Override
  public String generateToken(User user) {
    return jwtService.generateToken(user);
  }

  @Override
  public Boolean isTokenValid(Token token) {
    return token != null && isTokenExpired(token);
  }

  @Override
  public Boolean isTokenValid(String token) {
    Token tokenModel = tokenRepository.findByToken(token);
    return isTokenValid(tokenModel);
  }

  private boolean isTokenExpired(Token token) {
    return token.getExpiration().after(new Date());
  }
}
