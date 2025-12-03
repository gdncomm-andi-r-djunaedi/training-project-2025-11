package com.gdn.training.member.infrastructure.grpc;

import com.gdn.training.member.application.usecase.GetMemberProfileUseCase;
import com.gdn.training.member.application.usecase.LoginMemberUseCase;
import com.gdn.training.member.application.usecase.RegisterMemberUseCase;
import com.gdn.training.member.application.usecase.ValidateMemberUseCase;
import com.gdn.training.member.infrastructure.grpc.proto.MemberServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC implementation of MemberService
 */
@GrpcService
public class MemberServiceGrpcImpl extends MemberServiceGrpc.MemberServiceImplBase {
    private final RegisterMemberUseCase registerMemberUseCase;
    private final LoginMemberUseCase loginMemberUseCase;
    private final GetMemberProfileUseCase getMemberProfileUseCase;
    private final ValidateMemberUseCase validateMemberUseCase;

    public MemberServiceGrpcImpl(RegisterMemberUseCase registerMemberUseCase,
            LoginMemberUseCase loginMemberUseCase,
            GetMemberProfileUseCase getMemberProfileUseCase,
            ValidateMemberUseCase validateMemberUseCase) {
        this.registerMemberUseCase = registerMemberUseCase;
        this.loginMemberUseCase = loginMemberUseCase;
        this.getMemberProfileUseCase = getMemberProfileUseCase;
        this.validateMemberUseCase = validateMemberUseCase;
    }

    @Override
    public void registerMember(com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest request,
            StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse> responseObserver) {
        // Map proto -> application DTO
        com.gdn.training.member.application.dto.request.RegisterMemberRequest appRequest = MemberGrpcMapper
                .toRegisterRequest(request);

        try {
            // Delegate to usecase
            com.gdn.training.member.application.dto.response.RegisterMemberResponse appResponse = registerMemberUseCase
                    .register(appRequest);

            // Map application DTO -> proto
            com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse protoResponse = com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse
                    .newBuilder()
                    .setMemberId(appResponse.memberId().toString())
                    .setFullName(appResponse.fullName())
                    .setEmail(appResponse.email())
                    .build();

            // Send response
            responseObserver.onNext(protoResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onError(io.grpc.Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void loginMember(com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest request,
            StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse> responseObserver) {
        com.gdn.training.member.application.dto.request.LoginMemberRequest appRequest = MemberGrpcMapper
                .toLoginRequest(request);

        try {
            com.gdn.training.member.application.dto.response.LoginMemberResponse appResponse = loginMemberUseCase
                    .login(appRequest);

            com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse protoResponse = com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse
                    .newBuilder()
                    .setSuccess(appResponse.success())
                    .setMemberId(appResponse.memberId() != null ? appResponse.memberId().toString() : "")
                    .setFullName(appResponse.fullName() != null ? appResponse.fullName() : "")
                    .setEmail(appResponse.email() != null ? appResponse.email() : "")
                    .build();

            responseObserver.onNext(protoResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getMemberProfile(com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest request,
            StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse> responseObserver) {
        try {
            com.gdn.training.member.application.dto.response.MemberProfileResponse appResponse = getMemberProfileUseCase
                    .getProfile(java.util.UUID.fromString(request.getMemberId()));

            com.gdn.training.member.infrastructure.grpc.proto.MemberMessage memberMessage = MemberGrpcMapper
                    .toMemberMessage(appResponse);

            com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse protoResponse = com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse
                    .newBuilder()
                    .setMember(memberMessage)
                    .build();

            responseObserver.onNext(protoResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void validateMember(com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest request,
            StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse> responseObserver) {
        try {
            boolean exists = validateMemberUseCase.exists(java.util.UUID.fromString(request.getMemberId()));

            com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse protoResponse = com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse
                    .newBuilder()
                    .setExists(exists)
                    .build();

            responseObserver.onNext(protoResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
