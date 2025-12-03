package com.gdn.training.member.infrastructure.grpc;

import com.gdn.training.member.application.dto.response.MemberProfileResponse;
import com.gdn.training.member.application.dto.request.RegisterMemberRequest;
import com.gdn.training.member.application.dto.request.LoginMemberRequest;

/**
 * 
 */
public final class MemberGrpcMapper {

    private MemberGrpcMapper() {
    }

    public static com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest toProto(
            RegisterMemberRequest r) {
        return com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest.newBuilder()
                .setFullName(r.fullName())
                .setEmail(r.email())
                .setRawPassword(r.password())
                .setPhoneNumber(r.phoneNumber() == null ? "" : r.phoneNumber())
                .build();
    }

    public static RegisterMemberRequest toRegisterRequest(
            com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest r) {
        return new RegisterMemberRequest(r.getFullName(), r.getEmail(), r.getRawPassword(),
                r.getPhoneNumber().isEmpty() ? null : r.getPhoneNumber());
    }

    public static LoginMemberRequest toLoginRequest(
            com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest r) {
        return new LoginMemberRequest(r.getEmail(), r.getRawPassword());
    }

    public static com.gdn.training.member.infrastructure.grpc.proto.MemberMessage toMemberMessage(
            MemberProfileResponse resp) {
        if (resp == null) {
            return com.gdn.training.member.infrastructure.grpc.proto.MemberMessage.newBuilder().build();
        }
        com.gdn.training.member.infrastructure.grpc.proto.MemberMessage.Builder b = com.gdn.training.member.infrastructure.grpc.proto.MemberMessage
                .newBuilder();
        if (resp.memberId() != null)
            b.setId(resp.memberId().toString());
        if (resp.fullName() != null)
            b.setFullName(resp.fullName());
        if (resp.email() != null)
            b.setEmail(resp.email());
        if (resp.phoneNumber() != null)
            b.setPhoneNumber(resp.phoneNumber());
        // Note: MemberProfileResponse doesn't have address field, so we skip it
        // createdAt/updatedAt not available here (application DTO doesn't carry
        // timestamps)
        return b.build();
    }

}
