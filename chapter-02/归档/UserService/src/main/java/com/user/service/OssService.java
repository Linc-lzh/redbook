package com.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {

    String uploadFile(MultipartFile file);
}
