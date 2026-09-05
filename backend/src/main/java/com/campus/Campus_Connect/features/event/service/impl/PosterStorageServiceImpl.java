package com.campus.Campus_Connect.features.event.service.impl;

import com.campus.Campus_Connect.features.event.service.PosterStorageService;
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
public class PosterStorageServiceImpl implements PosterStorageService {

    private final Path uploadDirectory =
            Paths.get("uploads/posters").toAbsolutePath().normalize();

    @Override
    public String store(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Poster file is empty.");
        }

        Files.createDirectories(uploadDirectory);

        String originalFilename =
                StringUtils.cleanPath(file.getOriginalFilename());

        String extension = "";

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String filename = UUID.randomUUID() + extension;

        Path targetLocation =
                uploadDirectory.resolve(filename);

        Files.copy(
                file.getInputStream(),
                targetLocation,
                StandardCopyOption.REPLACE_EXISTING
        );

        return "/uploads/posters/" + filename;
    }

    @Override
    public void delete(String posterUrl) throws IOException {

        if (posterUrl == null || posterUrl.isBlank()) {
            return;
        }

        String filename =
                Paths.get(posterUrl).getFileName().toString();

        Path file =
                uploadDirectory.resolve(filename);

        Files.deleteIfExists(file);
    }
}