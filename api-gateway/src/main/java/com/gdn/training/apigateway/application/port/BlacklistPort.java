package com.gdn.training.apigateway.application.port;

public interface BlacklistPort {
    void blacklist(String jti);

    boolean isBlacklisted(String jti);
}
