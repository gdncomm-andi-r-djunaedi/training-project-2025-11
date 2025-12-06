package com.gdn.training.apigateway.application.usecase;

import com.gdn.training.apigateway.application.port.MemberGrpcPort;
import com.gdn.training.apigateway.application.usecase.model.RegisterResult;
import org.springframework.stereotype.Service;

@Service
public class RegisterMemberUseCase {
    private final MemberGrpcPort memberGrpcPort;

    public RegisterMemberUseCase(MemberGrpcPort memberGrpcPort) {
        this.memberGrpcPort = memberGrpcPort;
    }

    public RegisterResult execute(String fullName, String email, String password, String phoneNumber) {
        return memberGrpcPort.register(fullName, email, password, phoneNumber);
    }
}
