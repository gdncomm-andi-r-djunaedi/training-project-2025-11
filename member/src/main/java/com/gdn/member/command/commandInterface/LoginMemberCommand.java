package com.gdn.member.command.commandInterface;

import com.gdn.member.command.base.Command;
import com.gdn.member.command.model.LoginMemberCommandRequest;
import com.gdn.member.controller.webmodel.response.LoginMemberResponse;

public interface LoginMemberCommand extends Command<LoginMemberCommandRequest, LoginMemberResponse> {
}
