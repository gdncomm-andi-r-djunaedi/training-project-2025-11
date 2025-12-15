package com.blibli.member.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlackList {
    private List<String> blockList = Collections.synchronizedList(new ArrayList<>());
    public void addBlockListToken(String token){
        blockList.add(token);
    }
    public boolean isBlockedToken(String token){
        return blockList.contains(token);
    }
}
