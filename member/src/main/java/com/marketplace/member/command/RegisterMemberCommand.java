package com.marketplace.member.command;

import com.marketplace.common.command.Command;
import com.marketplace.member.dto.request.RegisterRequest;
import com.marketplace.member.dto.response.MemberResponse;

public interface RegisterMemberCommand extends Command<RegisterRequest, MemberResponse> {
}
