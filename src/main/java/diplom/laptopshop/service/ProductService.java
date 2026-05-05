package diplom.laptopshop.service;

import diplom.laptopshop.model.dto.ProductCreateDTO;
import diplom.laptopshop.model.dto.ProductResponseDTO;
import diplom.laptopshop.model.entity.Product;
import diplom.laptopshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    @Value("${cloudinary.folder:products}")
    private String cloudinaryFolder;

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO createDTO) {
        // Upload image to Cloudinary
        String imageUrl = cloudinaryService.uploadImage(
                createDTO.getImageUrl(),
                cloudinaryFolder
        );

        // Create product entity
        Product product = Product.builder()
                .price(createDTO.getPrice())
                .description(createDTO.getDescription())
                .imageUrl(imageUrl)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());

        return convertToResponseDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductCreateDTO updateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // If new image uploaded, delete old one and upload new
        if (updateDTO.getImageUrl() != null && !updateDTO.getImageUrl().isEmpty()) {
            cloudinaryService.deleteImage(product.getImageUrl());
            String newImageUrl = cloudinaryService.uploadImage(
                    updateDTO.getImageUrl(),
                    cloudinaryFolder
            );
            product.setImageUrl(newImageUrl);
        }

        product.setPrice(updateDTO.getPrice());
        product.setDescription(updateDTO.getDescription());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with ID: {}", updatedProduct.getId());

        return convertToResponseDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Delete image from Cloudinary
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            cloudinaryService.deleteImage(product.getImageUrl());
        }

        // Delete from database
        productRepository.delete(product);
        log.info("Product deleted with ID: {}", id);
    }

    private ProductResponseDTO convertToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .price(product.getPrice())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .build();
    }
}