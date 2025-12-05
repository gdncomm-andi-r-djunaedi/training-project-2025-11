package com.blibli.api_gateway.utils;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BlockListToken {
    private List<String> blockList = Collections.synchronizedList(new ArrayList<>());
    public void addBlockListToken(String token){
        blockList.add(token);
    }
    public boolean isBlockedToken(String token){
        return blockList.contains(token);
    }
}
