package com.gdn.member.utils;


import lombok.Data;

import java.security.Key;
import io.jsonwebtoken.security.Keys;

public interface Constants {
    static final String SECRET_STRING = "vD7w9J+Qbb0YV3YTZmXc7x0xgPpCSZ1wN3SzcFi8knc=";
    static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

}
