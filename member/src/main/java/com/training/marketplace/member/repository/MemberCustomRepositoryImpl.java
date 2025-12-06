package com.training.marketplace.member.repository;

import com.training.marketplace.member.entity.MemberEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;


public class MemberCustomRepositoryImpl implements MemberCustomRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Optional<MemberEntity> findUserByUsername(String username) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MemberEntity> criteriaQuery = criteriaBuilder.createQuery(MemberEntity.class);
        Root<MemberEntity> root = criteriaQuery.from(MemberEntity.class);

        Predicate condition = criteriaBuilder.like(root.get("username"),username);
        criteriaQuery.where(condition);

        TypedQuery<MemberEntity> typedQuery = entityManager.createQuery(criteriaQuery);
        return Optional.ofNullable(typedQuery.getSingleResultOrNull());
    }
}
