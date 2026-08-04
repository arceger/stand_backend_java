package com.stand.backend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
class StorageService {
    private final Path uploadDir;

    StorageService(@Value("${app.storage.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    Path getUploadDir() {
        return uploadDir;
    }

    StoredImage save(MultipartFile file, UUID vehicleId) {
        try {
            Files.createDirectories(uploadDir);
            String extension = extractExtension(file.getOriginalFilename());
            String safeName = slugify(file.getOriginalFilename());
            String storageKey = vehicleId + "-" + UUID.randomUUID() + "-" + safeName + extension;
            Path target = uploadDir.resolve(storageKey).normalize();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredImage(storageKey, "/uploads/" + storageKey);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel salvar a imagem enviada.", exception);
        }
    }

    void deleteIfManaged(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(uploadDir.resolve(storageKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel remover a imagem antiga.", exception);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private String slugify(String filename) {
        if (filename == null || filename.isBlank()) {
            return "imagem";
        }
        String normalized = Normalizer.normalize(filename, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]+", "-")
            .replaceAll("(^-|-$)", "")
            .toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? "imagem" : normalized;
    }

    record StoredImage(String storageKey, String publicUrl) {
    }
}
