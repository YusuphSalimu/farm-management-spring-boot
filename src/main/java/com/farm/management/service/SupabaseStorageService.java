package com.farm.management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    public String uploadProfilePicture(MultipartFile file,
                                       Long userId) throws Exception {

        String extension = getExtension(
                file.getOriginalFilename());
        String fileName = "user-" + userId + "-" +
                UUID.randomUUID() + "." + extension;

        String uploadUrl = supabaseUrl +
                "/storage/v1/object/" + bucket +
                "/" + fileName;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", file.getContentType())
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers
                        .ofByteArray(file.getBytes()))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 ||
                response.statusCode() == 201) {
            return supabaseUrl + "/storage/v1/object/public/"
                    + bucket + "/" + fileName;
        } else {
            throw new RuntimeException(
                    "Upload failed: " + response.body());
        }
    }

    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(
                    filename.lastIndexOf(".") + 1);
        }
        return "jpg";
    }

    public void deleteFile(String fileUrl) throws Exception {
        // Extract filename from full URL
        String fileName = fileUrl.substring(
                fileUrl.lastIndexOf("/") + 1);

        String deleteUrl = supabaseUrl +
                "/storage/v1/object/" + bucket + "/" + fileName;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .header("Authorization", "Bearer " + supabaseKey)
                .DELETE()
                .build();

        client.send(request,
                HttpResponse.BodyHandlers.ofString());
    }
}