package com.campus.Campus_Connect.common.service.impl;

import com.campus.Campus_Connect.common.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String UPLOAD_DIR = "uploads/posts";

    @Override
    public String storePostImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        try {

            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String extension =
                    StringUtils.getFilenameExtension(file.getOriginalFilename());

            String fileName =
                    UUID.randomUUID() + "." + extension;

            Path targetLocation = uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "uploads/posts/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store image.", ex);
        }
    }
}