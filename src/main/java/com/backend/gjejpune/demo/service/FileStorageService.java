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
    
    /**
     * Store a file in the default upload directory
     * 
     * @param file The file to store
     * @return The URL of the stored file
     */
    public String storeFile(MultipartFile file) {
        return storeFile(file, null);
    }
    
    /**
     * Store a file in a specific subdirectory of the upload directory
     * 
     * @param file The file to store
     * @param subDirectory The subdirectory to store the file in (e.g., "profile_images", "post_images")
     * @return The URL of the stored file
     */
    public String storeFile(MultipartFile file, String subDirectory) {
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
            
            // Create subdirectory if specified
            Path targetPath = this.fileStoragePath;
            if (subDirectory != null && !subDirectory.isEmpty()) {
                targetPath = this.fileStoragePath.resolve(subDirectory);
                Files.createDirectories(targetPath);
            }
            
            // Copy file to the target location (replacing existing file with the same name)
            Path targetLocation = targetPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Create a URL for the file
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/")
                    .path(subDirectory != null && !subDirectory.isEmpty() ? subDirectory + "/" : "")
                    .path(uniqueFileName)
                    .toUriString();
            
            logger.info("Stored file: {} as {} in {}", originalFileName, uniqueFileName, 
                    subDirectory != null ? subDirectory : "default directory");
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