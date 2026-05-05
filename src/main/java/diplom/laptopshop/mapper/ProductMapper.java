package diplom.laptopshop.mapper;

import diplom.laptopshop.model.dto.ProductCreateDTO;
import diplom.laptopshop.model.dto.ProductResponseDTO;
import diplom.laptopshop.model.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    public Product toEntity(ProductCreateDTO createDTO, String imageUrl) {
        if (createDTO == null) {
            return null;
        }

        return Product.builder()
                .price(createDTO.getPrice())
                .description(createDTO.getDescription())
                .imageUrl(imageUrl)
                .build();
    }

    public ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .price(product.getPrice())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .build();
    }

    public void updateEntity(Product existingProduct, ProductCreateDTO updateDTO, String newImageUrl) {
        if (existingProduct == null || updateDTO == null) {
            return;
        }

        existingProduct.setPrice(updateDTO.getPrice());
        existingProduct.setDescription(updateDTO.getDescription());

        if (newImageUrl != null) {
            existingProduct.setImageUrl(newImageUrl);
        }
    }
}