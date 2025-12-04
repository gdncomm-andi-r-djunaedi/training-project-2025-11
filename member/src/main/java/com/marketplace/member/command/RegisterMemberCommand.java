package com.marketplace.member.command;

import com.marketplace.common.command.Command;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;

public interface RegisterMemberCommand extends Command<RegisterRequest, MemberResponse> {
}
