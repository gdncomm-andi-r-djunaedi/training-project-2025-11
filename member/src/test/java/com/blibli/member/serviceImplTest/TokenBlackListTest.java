package com.blibli.member.serviceImplTest;

import com.blibli.member.service.impl.TokenBlackList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenBlackListTest {

    @InjectMocks
    TokenBlackList tokenBlackList;

    @Test
    public void test_addBlockListToken(){
        tokenBlackList.addBlockListToken("12345");
        boolean val = tokenBlackList.isBlockedToken("12345");
        boolean res = tokenBlackList.isBlockedToken("123");
        Assertions.assertTrue(val);
        Assertions.assertFalse(res);
    }
}
