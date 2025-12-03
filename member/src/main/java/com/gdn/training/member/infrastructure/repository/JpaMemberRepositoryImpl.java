package com.gdn.training.member.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.gdn.training.member.domain.model.Member;
import com.gdn.training.member.domain.port.out.MemberRepository;
import com.gdn.training.member.infrastructure.entity.MemberEntity;

import jakarta.transaction.Transactional;

@Repository
public class JpaMemberRepositoryImpl implements MemberRepository {
    private final SpringDataJpaMemberRepository springDataJpaMemberRepository;

    public JpaMemberRepositoryImpl(SpringDataJpaMemberRepository springDataJpaMemberRepository) {
        this.springDataJpaMemberRepository = springDataJpaMemberRepository;
    }

    @Override
    public Optional<Member> findById(UUID id) {
        return springDataJpaMemberRepository.findById(id).map(this::toMember);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return springDataJpaMemberRepository.findByEmail(email).map(this::toMember);
    }

    @Override
    @Transactional
    public Member save(Member member) {
        MemberEntity memberEntity = toMemberEntity(member);
        MemberEntity savedMemberEntity = springDataJpaMemberRepository.save(memberEntity);
        return toMember(savedMemberEntity);
    }

    @Override
    public long count() {
        return springDataJpaMemberRepository.count();
    }

    private Member toMember(MemberEntity memberEntity) {
        return new Member(memberEntity.getId(), memberEntity.getFullName(), memberEntity.getEmail(),
                memberEntity.getPasswordHash(), memberEntity.getPhoneNumber(), memberEntity.getAvatarUrl(),
                memberEntity.getCreatedAt(), memberEntity.getUpdatedAt());
    }

    private MemberEntity toMemberEntity(Member member) {
        return new MemberEntity(member.getId(), member.getFullName(), member.getEmail(), member.getPasswordHash(),
                member.getPhoneNumber(), member.getAvatarUrl(), member.getCreatedAt(), member.getUpdatedAt());
    }
}
