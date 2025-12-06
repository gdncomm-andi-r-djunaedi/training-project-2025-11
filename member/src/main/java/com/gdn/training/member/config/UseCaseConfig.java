package com.gdn.training.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gdn.training.member.application.usecase.GetMemberProfileUseCase;
import com.gdn.training.member.application.usecase.LoginMemberUseCase;
import com.gdn.training.member.application.usecase.RegisterMemberUseCase;
import com.gdn.training.member.application.usecase.ValidateMemberUseCase;
import com.gdn.training.member.application.usecase.impl.GetMemberProfileUseCaseImpl;
import com.gdn.training.member.application.usecase.impl.LoginMemberUseCaseImpl;
import com.gdn.training.member.application.usecase.impl.RegisterMemberUseCaseImpl;
import com.gdn.training.member.domain.password.PasswordHasher;
import com.gdn.training.member.domain.port.out.MemberRepository;

/**
 * Wiring of application use cases implementation as Spring beans
 */
@Configuration
public class UseCaseConfig {

    private final MemberRepository memberRepository;
    private final PasswordHasher passwordHasher;

    public UseCaseConfig(MemberRepository memberRepository, PasswordHasher passwordHasher) {
        this.memberRepository = memberRepository;
        this.passwordHasher = passwordHasher;
    }

    @Bean
    public RegisterMemberUseCase registerMemberUseCase() {
        return new RegisterMemberUseCaseImpl(memberRepository, passwordHasher);
    }

    @Bean
    public LoginMemberUseCase loginMemberUseCase() {
        return new LoginMemberUseCaseImpl(memberRepository, passwordHasher);
    }

    @Bean
    public GetMemberProfileUseCase getMemberByIdUseCase() {
        return new GetMemberProfileUseCaseImpl(memberRepository);
    }

    @Bean
    public ValidateMemberUseCase validateMemberUseCase() {
        return memberId -> memberRepository.findById(memberId)
                .isPresent();
    }
}
