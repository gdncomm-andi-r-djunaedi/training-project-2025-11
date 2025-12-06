package com.gdn.training.apigateway.unit;

import com.gdn.training.apigateway.application.usecase.LoginMemberUseCase;
import com.gdn.training.apigateway.infrastructure.security.JwtTokenProvider;
import com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest;
import com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse;
import com.gdn.training.member.infrastructure.grpc.proto.MemberServiceGrpc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginMemberUseCaseTest {

        private MemberServiceGrpc.MemberServiceBlockingStub memberStub;
        private JwtTokenProvider jwtTokenProvider;
        private LoginMemberUseCase useCase;

        @BeforeEach
        void setup() {
                memberStub = mock(MemberServiceGrpc.MemberServiceBlockingStub.class);
                jwtTokenProvider = mock(JwtTokenProvider.class);
                useCase = new LoginMemberUseCase(memberStub, jwtTokenProvider);
        }

        @Test
        void login_success_returnsJwtToken() {
                // ARRANGE
                String email = "user@example.com";
                String password = "secret";

                LoginMemberResponse grpcResponse = LoginMemberResponse.newBuilder()
                                .setSuccess(true)
                                .setMemberId("member-123")
                                .build();

                when(memberStub.loginMember(any(LoginMemberRequest.class)))
                                .thenReturn(grpcResponse);

                when(jwtTokenProvider.generateToken("member-123"))
                                .thenReturn("jwt-token-xyz");

                // ACT
                String token = useCase.login(email, password);

                // ASSERT
                assertEquals("jwt-token-xyz", token);

                verify(memberStub, times(1)).loginMember(any(LoginMemberRequest.class));
                verify(jwtTokenProvider, times(1)).generateToken("member-123");
        }

        @Test
        void login_invalidCredentials_throwsException() {
                // ARRANGE
                LoginMemberResponse grpcFailResponse = LoginMemberResponse.newBuilder()
                                .setSuccess(false)
                                .build();

                when(memberStub.loginMember(any(LoginMemberRequest.class)))
                                .thenReturn(grpcFailResponse);

                // ASSERT
                assertThrows(RuntimeException.class,
                                () -> useCase.login("user@example.com", "wrong-password"));

                verify(jwtTokenProvider, never()).generateToken(anyString());
        }
}
