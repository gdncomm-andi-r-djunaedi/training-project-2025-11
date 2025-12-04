package com.gdn.training.apigateway.application.usecase;

import com.gdn.training.apigateway.infrastructure.security.JwtTokenProvider;
import com.gdn.training.member.infrastructure.grpc.proto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginMemberUseCase {

    private final MemberServiceGrpc.MemberServiceBlockingStub memberStub;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(String email, String password) {

        LoginMemberRequest request = LoginMemberRequest.newBuilder()
                .setEmail(email)
                .setRawPassword(password)
                .build();

        LoginMemberResponse grpcResponse = memberStub.loginMember(request);

        if (!grpcResponse.getSuccess()) {
            throw new RuntimeException("Invalid login");
        }

        return jwtTokenProvider.generateToken(grpcResponse.getMemberId());
    }
}
