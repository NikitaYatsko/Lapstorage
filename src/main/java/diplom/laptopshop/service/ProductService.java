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
import org.springframework.web.multipart.MultipartFile;

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
        // Validate and upload image
        MultipartFile imageFile = createDTO.getImageUrl();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new RuntimeException("Image file is required");
        }

        String imageUrl = cloudinaryService.uploadImage(imageFile, cloudinaryFolder);

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

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductCreateDTO updateDTO) {
        // Find existing product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update price and description
        product.setPrice(updateDTO.getPrice());
        product.setDescription(updateDTO.getDescription());

        // Handle image update if new file provided
        MultipartFile newImageFile = updateDTO.getImageUrl();
        if (newImageFile != null && !newImageFile.isEmpty()) {
            // Delete old image from Cloudinary
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                cloudinaryService.deleteImage(product.getImageUrl());
                log.info("Old image deleted: {}", product.getImageUrl());
            }

            // Upload new image
            String newImageUrl = cloudinaryService.uploadImage(newImageFile, cloudinaryFolder);
            product.setImageUrl(newImageUrl);
            log.info("New image uploaded: {}", newImageUrl);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with ID: {}", updatedProduct.getId());

        return convertToResponseDTO(updatedProduct);
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
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Delete image from Cloudinary
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            cloudinaryService.deleteImage(product.getImageUrl());
            log.info("Image deleted from Cloudinary: {}", product.getImageUrl());
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