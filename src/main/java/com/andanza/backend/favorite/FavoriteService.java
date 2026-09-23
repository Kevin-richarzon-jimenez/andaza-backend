package com.andanza.backend.favorite;

import com.andanza.backend.catalog.CatalogService;
import org.springframework.stereotype.Service;

// Sin BD ni sesión: cada llamada valida que el producto exista de verdad
// (contra el catálogo) y responde con la acción realizada; no hay una
// lista real persistida por usuario todavía.
// TODO (BD): reemplazar por una tabla "favorite" (user_id, product_id) y
// acá mismo, agregar/eliminar la fila real según corresponda.
@Service
public class FavoriteService {

    private final CatalogService catalogService;

    public FavoriteService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public String toggle(FavoriteRequest request) {
        catalogService.findById(request.productId()); // lanza BusinessException si no existe

        // TODO (BD): si ya existe el favorito, eliminarlo; si no existe, crearlo.
        return "Actualizado en favoritos";
    }
}
