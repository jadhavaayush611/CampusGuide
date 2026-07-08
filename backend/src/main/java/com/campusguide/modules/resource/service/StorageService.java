package com.campusguide.modules.resource.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    /**
     * Stores the uploaded file physically.
     *
     * @param file the MultipartFile to store
     * @return the generated unique stored filename
     * @throws IOException if an I/O error occurs
     */
    String store(MultipartFile file) throws IOException;

    /**
     * Deletes the stored file physically.
     *
     * @param storedFileName the name of the file to delete
     */
    void delete(String storedFileName);

    /**
     * Checks if the stored file exists physically.
     *
     * @param storedFileName the name of the file to check
     * @return true if the file exists, false otherwise
     */
    boolean exists(String storedFileName);

    /**
     * Loads the stored file as a Resource.
     *
     * @param storedFileName the name of the file to load
     * @return the Resource representing the file
     * @throws IOException if the file cannot be loaded
     */
    Resource loadAsResource(String storedFileName) throws IOException;

    /**
     * Generates a unique stored filename preserving the original file extension.
     *
     * @param originalFileName the original name of the file
     * @return the generated unique stored filename
     */
    String generateStoredFileName(String originalFileName);
}
