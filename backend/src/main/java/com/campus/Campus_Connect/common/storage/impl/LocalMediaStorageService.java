package com.campus.Campus_Connect.common.storage.impl;

import com.campus.Campus_Connect.common.storage.MediaStorageService;
import com.campus.Campus_Connect.common.storage.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalMediaStorageService implements MediaStorageService {

    private final Path uploadRoot;

    public LocalMediaStorageService(
            @Value("${app.storage.local.root:uploads}") String uploadRoot
    ) {
        this.uploadRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not initialize media storage",
                    e
            );
        }
    }

    @Override
    public String store(MultipartFile file, MediaType type) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            String originalFilename = file.getOriginalFilename();

            String extension = "";

            if (originalFilename != null) {
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex >= 0) {
                    extension = originalFilename.substring(dotIndex);
                }
            }

            String filename = UUID.randomUUID() + extension;

            Path directory = uploadRoot.resolve(type.getDirectory());
            Files.createDirectories(directory);

            Path target = directory.resolve(filename).normalize();

            if (!target.startsWith(directory)) {
                throw new IllegalArgumentException("Invalid file path");
            }

            Files.copy(
                    file.getInputStream(),
                    target
            );

            return "/uploads/"
                    + type.getDirectory()
                    + "/"
                    + filename;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to store media file",
                    e
            );
        }
    }

    @Override
    public void delete(String url) {

        if (url == null || url.isBlank()) {
            return;
        }

        try {
            String prefix = "/uploads/";

            if (!url.startsWith(prefix)) {
                return;
            }

            String relativePath = url.substring(prefix.length());

            Path target = uploadRoot
                    .resolve(relativePath)
                    .normalize();

            if (!target.startsWith(uploadRoot)) {
                return;
            }

            Files.deleteIfExists(target);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to delete media file",
                    e
            );
        }
    }
}