package com.campus.Campus_Connect.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    String store(MultipartFile file, MediaType type);

    String copy(String storedUrl, MediaType type);

    void delete(String url);
}
