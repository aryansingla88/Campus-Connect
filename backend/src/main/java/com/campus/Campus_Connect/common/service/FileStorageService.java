package com.campus.Campus_Connect.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storePostImage(MultipartFile file);

}