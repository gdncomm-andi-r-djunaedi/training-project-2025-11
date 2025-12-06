package com.gdn.training.apigateway.application.usecase;

import com.gdn.training.apigateway.application.port.MemberGrpcPort;
import com.gdn.training.apigateway.application.usecase.model.MemberProfile;
import com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest;
import com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse;
import com.gdn.training.member.infrastructure.grpc.proto.MemberServiceGrpc;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Fetch member profile from member-service via gRPC.
 */
@Service
@RequiredArgsConstructor
public class GetMemberProfileUseCase {
    private final MemberServiceGrpc.MemberServiceBlockingStub memberStub;

    public GetMemberProfileResponse get(String memberId) {

        GetMemberProfileRequest request = GetMemberProfileRequest.newBuilder().setMemberId(memberId).build();

        return memberStub.getMemberProfile(request);
    }
}
