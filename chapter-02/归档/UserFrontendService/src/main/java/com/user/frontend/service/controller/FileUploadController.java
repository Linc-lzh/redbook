package com.user.frontend.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@RestController
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDirectory;

    // 文件类型白名单
    private final List<String> allowedFileTypes = Arrays.asList("image/png", "image/jpeg", "image/jpg");

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDirectory));
        } catch (IOException e) {
            throw new RuntimeException("无法初始化存储文件夹", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ResponseEntity.status(400).body("错误：没有选择文件！");
        }

        // 检查文件类型
        String fileType = file.getContentType();
        if (fileType == null || !allowedFileTypes.contains(fileType)) {
            return ResponseEntity.status(400).body("错误：不支持的文件类型！");
        }

        // 检查文件大小，此处限制为5MB
        long maxFileSizeInBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxFileSizeInBytes) {
            return ResponseEntity.status(400).body("错误：文件太大！");
        }

        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.contains("..")) {
                // 此处可以添加日志记录
                return ResponseEntity.status(400).body("错误：文件名不合法！");
            }

            Path targetLocation = Paths.get(uploadDirectory + File.separator + fileName);

            // 使用try-with-resources确保输入流关闭
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            return ResponseEntity.ok(fileDownloadUri);

        } catch (IOException ex) {
            ex.printStackTrace();
            // 在这里可以添加更多的异常处理逻辑
            System.err.println("文件上传时遇到错误: " + ex.getMessage());
            return ResponseEntity.status(500).body("错误：无法存储文件 " + file.getOriginalFilename() + ". 请稍后再试！");
        }
    }
}

