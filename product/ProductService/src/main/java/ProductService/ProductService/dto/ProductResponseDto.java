package ProductService.ProductService.dto;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductResponseDto {
    private String id;
    private String name;
    private String description;
    private double price;
    private LocalDate createdAt;
}
