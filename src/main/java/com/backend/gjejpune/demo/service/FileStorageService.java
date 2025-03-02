package com.backend.gjejpune.demo.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    private Path fileStoragePath;
    
    @PostConstruct
    public void init() {
        this.fileStoragePath = Paths.get(uploadDir)
                .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStoragePath);
        } catch (Exception ex) {
            logger.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    
    public String storeFile(MultipartFile file) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }
            
            // Generate a unique file name to prevent duplicates
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Copy file to the target location (replacing existing file with the same name)
            Path targetLocation = this.fileStoragePath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Create a URL for the file
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/")
                    .path(uniqueFileName)
                    .toUriString();
            
            logger.info("Stored file: {} as {}", originalFileName, uniqueFileName);
            return fileUrl;
            
        } catch (IOException ex) {
            logger.error("Could not store file {}. Error: {}", originalFileName, ex.getMessage());
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }
    
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                logger.error("File not found: {}", fileName);
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            logger.error("File not found: {}. Error: {}", fileName, ex.getMessage());
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }
} 