package com.andanza.backend.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

// Guarda archivos en un bucket público de Supabase Storage usando su API REST. El disco del servidor no sirve
// para esto: en el plan gratuito de Render se borra en cada reinicio. La llave de servicio da acceso total al
// proyecto, así que solo vive en variables de entorno y nunca sale del backend.
@Component
public class SupabaseStorage {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorage.class);
    private static final String ONE_YEAR_CACHE = "max-age=31536000";

    private final String baseUrl;
    private final String bucket;
    private final String serviceKey;
    private final RestClient client;

    public SupabaseStorage(@Value("${app.storage.supabase-url:}") String supabaseUrl,
                           @Value("${app.storage.service-key:}") String serviceKey,
                           @Value("${app.storage.bucket:product-images}") String bucket) {
        this.baseUrl = supabaseUrl.strip().replaceAll("/+$", "") + "/storage/v1";
        this.serviceKey = serviceKey.strip();
        this.bucket = bucket.strip();
        this.client = supabaseUrl.isBlank() ? null : RestClient.builder().baseUrl(baseUrl).build();
    }

    public boolean isConfigured() {
        return client != null && !serviceKey.isEmpty();
    }

    // Dirección pública del archivo (el bucket es público, no lleva firma).
    public String publicUrl(String path) {
        return baseUrl + "/object/public/" + bucket + "/" + path;
    }

    // Lo contrario de publicUrl: la ruta del archivo dentro del bucket, o null si la dirección no es de este bucket.
    public String pathOf(String publicUrl) {
        String prefix = publicUrl("");
        return publicUrl != null && publicUrl.startsWith(prefix) ? publicUrl.substring(prefix.length()) : null;
    }

    public void upload(String path, byte[] content, String contentType) {
        requireConfigured();
        try {
            // La ruta se concatena tal cual: como variable de la plantilla, sus "/" se codificarían como %2F y el
            // archivo quedaría guardado con un nombre distinto al de su dirección pública.
            client.post()
                    .uri("/object/" + bucket + "/" + path)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .header("Cache-Control", ONE_YEAR_CACHE)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("No se pudo subir {} a Supabase Storage", path, e);
            throw new StorageException(HttpStatus.BAD_GATEWAY, "No pudimos guardar la imagen. Inténtalo de nuevo.");
        }
    }

    // Borra sin fallar: un archivo huérfano en el bucket es un desperdicio, no un error para el usuario.
    public void deleteQuietly(List<String> paths) {
        if (paths.isEmpty() || !isConfigured()) {
            return;
        }
        try {
            client.method(HttpMethod.DELETE)
                    .uri("/object/{bucket}", bucket)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", paths))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("No se pudieron borrar {} archivo(s) de Supabase Storage", paths.size(), e);
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new StorageException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Las imágenes todavía no están configuradas en el servidor.");
        }
    }
}
