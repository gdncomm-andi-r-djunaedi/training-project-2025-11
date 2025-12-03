package com.gdn.faurihakim.member.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.member.command.model.GetMemberCommandRequest;
import com.gdn.faurihakim.member.web.model.response.GetMemberWebResponse;

public interface GetMemberCommand extends Command<GetMemberCommandRequest, GetMemberWebResponse> {
}
