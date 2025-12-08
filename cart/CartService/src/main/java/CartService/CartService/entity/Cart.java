package CartService.CartService.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "carts")
public class Cart {

    @Id
    private String userId;

    private List<CartItem> items = new ArrayList<>();
}

