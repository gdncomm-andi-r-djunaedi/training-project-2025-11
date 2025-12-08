package ProductService.ProductService.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

        @Id
        private String id;

        @Indexed
        private String name;
        private String description;
        private double price;

        @CreatedDate
        private LocalDate createdAt;

        public void generateId() {
                if (this.id == null) {
                        this.id = UUID.randomUUID().toString();
                }
        }
}

