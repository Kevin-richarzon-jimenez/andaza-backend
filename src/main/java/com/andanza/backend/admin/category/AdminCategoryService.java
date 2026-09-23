package com.andanza.backend.admin.category;

import com.andanza.backend.catalog.CatalogService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Sin BD: las categorías en sí viven en CatalogService (la misma lista que
// usa Admin - Productos para validar), acá solo se lleva el mapeo id ->
// nombre para que delete() sepa qué nombre liberar. create/delete son
// "synchronized" porque este service es un singleton compartido entre
// requests concurrentes.
// TODO (BD): reemplazar por una tabla "category" real.
@Service
public class AdminCategoryService {

    private final CatalogService catalogService;
    private final Map<String, String> categoryIds = new HashMap<>();

    public AdminCategoryService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public synchronized String create(AdminCategoryRequest request) {
        String name = request.name().trim();
        catalogService.addCategory(name); // lanza BusinessException si el nombre ya existe

        String id = "cat-" + UUID.randomUUID().toString().substring(0, 8);
        categoryIds.put(id, name);
        // TODO (BD): persistir la categoría.
        return id;
    }

    public synchronized void delete(String categoryId) {
        // TODO (BD): validar que la categoría no tenga productos asociados
        // antes de eliminarla, y eliminarla realmente de la tabla.
        String name = categoryIds.remove(categoryId);
        if (name != null) {
            catalogService.removeCategory(name);
        }
    }
}
