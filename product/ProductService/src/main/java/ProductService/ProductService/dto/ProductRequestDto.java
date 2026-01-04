package ProductService.ProductService.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductRequestDto {
    private String name;
    private String description;
    private double price;
}
