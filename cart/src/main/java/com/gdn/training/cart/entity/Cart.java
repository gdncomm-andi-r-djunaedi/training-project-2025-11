package com.gdn.training.cart.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Cart {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    @UuidGenerator
    private UUID id;

    @Column(name = "member_id", nullable = false, unique = true)
    private String memberId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();
}
