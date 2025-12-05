package com.gdn.member.command.commandInterface;

import com.gdn.member.command.base.Command;
import com.gdn.member.command.model.RegisterMemberCommandRequest;
import com.gdn.member.controller.webmodel.response.RegisterMemberResponse;

public interface RegisterMemberCommand extends Command<RegisterMemberCommandRequest, RegisterMemberResponse> {
}
