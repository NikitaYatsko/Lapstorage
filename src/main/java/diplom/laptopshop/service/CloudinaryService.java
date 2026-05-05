package diplom.laptopshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds 5MB limit");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }

            // Upload to Cloudinary
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "use_filename", true,
                    "unique_filename", true
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    uploadParams
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully, URL: {}", imageUrl);

            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isBlank()) {
                return;
            }

            // Extract public_id from URL
            // Example URL: https://res.cloudinary.com/cloud_name/image/upload/v123456/products/filename.jpg
            String publicId = extractPublicIdFromUrl(imageUrl);

            if (publicId != null) {
                Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary", e);
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        // Extract public_id from Cloudinary URL
        // This is a simple implementation - adjust based on your URL structure
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String pathAndFile = parts[1];
                // Remove version prefix (v123456789/)
                if (pathAndFile.contains("/v") && pathAndFile.indexOf("/v") == 0) {
                    pathAndFile = pathAndFile.substring(pathAndFile.indexOf("/", 2) + 1);
                }
                // Remove file extension
                int dotIndex = pathAndFile.lastIndexOf(".");
                if (dotIndex > 0) {
                    pathAndFile = pathAndFile.substring(0, dotIndex);
                }
                return pathAndFile;
            }
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", imageUrl);
        }
        return null;
    }
}