package org.jovian.cart.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "carts")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
public class Cart {

    @Id
    private Long id;

    @ElementCollection
    private List<CartItem> productIds;
}

