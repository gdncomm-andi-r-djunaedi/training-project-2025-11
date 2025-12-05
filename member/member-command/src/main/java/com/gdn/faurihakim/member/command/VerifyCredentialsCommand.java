package com.gdn.faurihakim.member.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.member.command.model.VerifyCredentialsCommandRequest;
import com.gdn.faurihakim.member.web.model.response.VerifyCredentialsWebResponse;

public interface VerifyCredentialsCommand
        extends Command<VerifyCredentialsCommandRequest, VerifyCredentialsWebResponse> {
}
