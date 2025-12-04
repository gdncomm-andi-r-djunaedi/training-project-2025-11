package com.gdn.training.apigateway.application.usecase;

import com.gdn.training.apigateway.infrastructure.security.TokenBlacklist;
import com.gdn.training.member.infrastructure.grpc.proto.Empty;
import com.gdn.training.member.infrastructure.grpc.proto.MemberServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutMemberUseCase {
    private final TokenBlacklist blacklist;
    private final MemberServiceGrpc.MemberServiceBlockingStub memberStub;

    public void logout(String token) {

        // Add to blacklist
        blacklist.blacklist(token);

        // Optional: notify member-service
        memberStub.logoutMember(Empty.getDefaultInstance());
    }
}
