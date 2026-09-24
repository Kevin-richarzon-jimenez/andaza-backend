package com.andanza.backend.favorite;

import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.catalog.ProductResponse;
import com.andanza.backend.exception.NotFoundException;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, ProductRepository productRepository,
                           UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(UUID userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(favorite -> ProductResponse.from(favorite.getProduct()))
                .toList();
    }

    // Idempotente: agregar un favorito que ya existe no es un error.
    @Transactional
    public void add(UUID userId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("productId", "El producto indicado no existe"));
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user", "El usuario no existe"));
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        favoriteRepository.save(favorite);
    }

    // Idempotente: quitar un favorito que no existe tampoco es un error.
    @Transactional
    public void remove(UUID userId, UUID productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
