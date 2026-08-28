package com.stand.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class StorageService {

    private final Cloudinary cloudinary;

    public StorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public StoredImage save(MultipartFile file, UUID vehicleId) {
        try {
            // Envia o array de bytes
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "vehicles",
                            "public_id", vehicleId + "-" + UUID.randomUUID()
                    )
            );

            // public_id retornado
            String storageKey = uploadResult.get("public_id").toString();
            String publicUrl = uploadResult.get("secure_url").toString();

            return new StoredImage(storageKey, publicUrl);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel salvar a imagem no Cloudinary.", exception);
        }
    }

    public void deleteIfManaged(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(storageKey, ObjectUtils.emptyMap());
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel remover a imagem no Cloudinary.", exception);
        }
    }

    public record StoredImage(String storageKey, String publicUrl) {
    }
}
