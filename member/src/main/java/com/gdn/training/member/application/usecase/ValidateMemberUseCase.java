package com.gdn.training.member.application.usecase;

import java.util.UUID;

public interface ValidateMemberUseCase {
    boolean exists(UUID memberId);
}
