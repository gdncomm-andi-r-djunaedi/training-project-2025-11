package com.gdn.faurihakim.cart.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.cart.command.model.GetMemberCartCommandRequest;
import com.gdn.faurihakim.cart.web.model.response.GetMemberCartWebResponse;

public interface GetMemberCartCommand extends Command<GetMemberCartCommandRequest, GetMemberCartWebResponse> {
}
