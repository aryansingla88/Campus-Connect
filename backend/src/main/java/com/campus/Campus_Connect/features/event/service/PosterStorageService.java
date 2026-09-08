package com.campus.Campus_Connect.features.event.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PosterStorageService {

    String store(MultipartFile file) throws IOException;

    void delete(String posterUrl) throws IOException;
}