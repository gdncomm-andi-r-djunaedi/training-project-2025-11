package com.blibli.cart.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@Document(collection = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    @Id
    private String userId; // UUID from Member service (also MongoDB ID)

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private Date createdAt;
    private Date updatedAt;
}
