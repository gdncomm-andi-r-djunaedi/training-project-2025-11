package com.marketplace.member.command;

import com.marketplace.common.command.Command;
import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;

public interface ValidateCredentialsCommand extends Command<ValidateCredentialsRequest, UserDetailsResponse> {
}
