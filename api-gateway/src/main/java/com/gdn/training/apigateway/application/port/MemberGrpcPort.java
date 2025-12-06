package com.gdn.training.apigateway.application.port;

import com.gdn.training.apigateway.application.usecase.model.LoginResult;
import com.gdn.training.apigateway.application.usecase.model.RegisterResult;
import com.gdn.training.apigateway.application.usecase.model.MemberProfile;

public interface MemberGrpcPort {

    RegisterResult register(String fullName, String email, String rawPassword, String phoneNumber);

    LoginResult login(String email, String rawPassword);

    boolean validateMember(String memberId);

    void logout();

    MemberProfile getMemberProfile(String memberId);
}
