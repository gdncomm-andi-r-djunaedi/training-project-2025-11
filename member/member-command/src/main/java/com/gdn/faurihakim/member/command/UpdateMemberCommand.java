
package com.gdn.faurihakim.member.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.member.command.model.UpdateMemberCommandRequest;
import com.gdn.faurihakim.member.web.model.response.UpdateMemberWebResponse;

public interface UpdateMemberCommand extends Command<UpdateMemberCommandRequest, UpdateMemberWebResponse> {
}
