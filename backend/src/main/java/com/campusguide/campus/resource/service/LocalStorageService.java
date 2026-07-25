package com.campusguide.campus.resource.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    public LocalStorageService(@Value("${storage.location:uploads/resources}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains("..")) {
            throw new SecurityException("Cannot store file with relative path outside current directory");
        }

        String storedFilename = generateStoredFileName(originalFilename);
        Path destinationFile = this.rootLocation.resolve(storedFilename).normalize().toAbsolutePath();

        // Prevent path traversal attacks and validate destination path
        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new SecurityException("Cannot store file outside current directory.");
        }

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedFilename;
    }

    @Override
    public void delete(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }
        try {
            Path file = this.rootLocation.resolve(storedFileName).normalize().toAbsolutePath();
            // Prevent path traversal attacks
            if (!file.getParent().equals(this.rootLocation)) {
                throw new SecurityException("Cannot delete file outside current directory.");
            }
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + storedFileName, e);
        }
    }

    @Override
    public boolean exists(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return false;
        }
        Path file = this.rootLocation.resolve(storedFileName).normalize().toAbsolutePath();
        // Prevent path traversal attacks
        if (!file.getParent().equals(this.rootLocation)) {
            return false;
        }
        return Files.exists(file);
    }

    @Override
    public Resource loadAsResource(String storedFileName) throws IOException {
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new IllegalArgumentException("Stored file name cannot be null or empty.");
        }
        try {
            Path file = this.rootLocation.resolve(storedFileName).normalize().toAbsolutePath();
            // Prevent path traversal attacks
            if (!file.getParent().equals(this.rootLocation)) {
                throw new SecurityException("Cannot read file outside current directory.");
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("Could not read file: " + storedFileName);
            }
        } catch (MalformedURLException e) {
            throw new IOException("Could not read file: " + storedFileName, e);
        }
    }

    @Override
    public String generateStoredFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        if (originalFileName == null || originalFileName.isBlank()) {
            return uuid;
        }
        int extensionIndex = originalFileName.lastIndexOf('.');
        if (extensionIndex == -1) {
            return uuid;
        }
        String extension = originalFileName.substring(extensionIndex);
        return uuid + extension;
    }
}
