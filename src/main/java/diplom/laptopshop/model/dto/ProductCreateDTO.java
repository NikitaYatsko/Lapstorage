package diplom.laptopshop.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @DecimalMax(value = "99999999.99", message = "Price cannot exceed 99,999,999.99")
    private BigDecimal price;

    @Size(max = 5000, message = "Description is too long (max 5000 characters)")
    private String description;

    @NotNull
    private MultipartFile imageUrl;
}