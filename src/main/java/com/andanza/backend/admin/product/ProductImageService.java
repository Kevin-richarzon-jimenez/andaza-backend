package com.andanza.backend.admin.product;

import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductImage;
import com.andanza.backend.catalog.ProductImageRepository;
import com.andanza.backend.catalog.ProductImageResponse;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.catalog.ProductVariant;
import com.andanza.backend.exception.BusinessException;
import com.andanza.backend.exception.NotFoundException;
import com.andanza.backend.storage.SupabaseStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

// Las fotos de un producto van por color: las tallas de un mismo color comparten galería. El navegador manda cada
// foto ya reducida a WebP en dos tamaños (la grande para la ficha y una miniatura para las tarjetas); aquí solo se
// valida y se guarda.
@Service
public class ProductImageService {

    static final int MAX_IMAGES_PER_COLOR = 5;
    static final long MAX_IMAGE_BYTES = 800 * 1024;
    static final long MAX_THUMBNAIL_BYTES = 200 * 1024;
    private static final String WEBP = "image/webp";

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final SupabaseStorage storage;

    public ProductImageService(ProductRepository productRepository, ProductImageRepository imageRepository,
                               SupabaseStorage storage) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.storage = storage;
    }

    @Transactional
    public ProductImageResponse add(UUID productId, String color, MultipartFile image, MultipartFile thumbnail) {
        Product product = findProduct(productId);
        String variantColor = product.getVariants().stream()
                .map(ProductVariant::getColor)
                .filter(candidate -> candidate.equalsIgnoreCase(color.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("color", "El producto no tiene el color " + color.trim()));

        List<ProductImage> existing = imageRepository
                .findByProductIdAndColorIgnoreCaseOrderBySortOrderAscIdAsc(productId, variantColor);
        if (existing.size() >= MAX_IMAGES_PER_COLOR) {
            throw new BusinessException("image", "Cada color admite hasta " + MAX_IMAGES_PER_COLOR + " imágenes");
        }
        byte[] imageBytes = readWebp(image, "image", MAX_IMAGE_BYTES);
        byte[] thumbnailBytes = readWebp(thumbnail, "thumbnail", MAX_THUMBNAIL_BYTES);

        String fileName = UUID.randomUUID().toString();
        String imagePath = "products/" + productId + "/" + fileName + ".webp";
        String thumbnailPath = "products/" + productId + "/" + fileName + "-thumb.webp";

        ProductImage entity = new ProductImage();
        entity.setProduct(product);
        entity.setColor(variantColor);
        entity.setSortOrder(existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1);
        entity.setUrl(storage.publicUrl(imagePath));
        entity.setThumbnailUrl(storage.publicUrl(thumbnailPath));

        // Primero los archivos y después la fila: si algo falla, no queda una imagen registrada sin archivo.
        storage.upload(imagePath, imageBytes, WEBP);
        try {
            storage.upload(thumbnailPath, thumbnailBytes, WEBP);
            return ProductImageResponse.from(imageRepository.saveAndFlush(entity));
        } catch (RuntimeException e) {
            storage.deleteQuietly(List.of(imagePath, thumbnailPath));
            throw e;
        }
    }

    @Transactional
    public void remove(UUID productId, UUID imageId) {
        findProduct(productId);
        ProductImage image = findImage(productId, imageId);
        imageRepository.delete(image);
        deleteFilesAfterCommit(List.of(image));
    }

    // La imagen elegida pasa a ser la primera (la portada) de su color; las demás conservan su orden.
    @Transactional
    public List<ProductImageResponse> makeCover(UUID productId, UUID imageId) {
        findProduct(productId);
        ProductImage chosen = findImage(productId, imageId);
        List<ProductImage> ordered = new ArrayList<>(imageRepository
                .findByProductIdAndColorIgnoreCaseOrderBySortOrderAscIdAsc(productId, chosen.getColor()));
        ordered.removeIf(image -> image.getId().equals(chosen.getId()));
        ordered.add(0, chosen);
        for (int position = 0; position < ordered.size(); position++) {
            ordered.get(position).setSortOrder(position);
        }
        return ordered.stream().map(ProductImageResponse::from).toList();
    }

    // Al borrar un producto, sus archivos se borran del almacenamiento cuando la transacción se confirma.
    public void deleteFilesOfProductAfterCommit(Product product) {
        deleteFilesAfterCommit(List.copyOf(product.getImages()));
    }

    private void deleteFilesAfterCommit(List<ProductImage> images) {
        List<String> paths = images.stream()
                .flatMap(image -> Stream.of(storage.pathOf(image.getUrl()), storage.pathOf(image.getThumbnailUrl())))
                .filter(Objects::nonNull)
                .toList();
        if (paths.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            storage.deleteQuietly(paths);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storage.deleteQuietly(paths);
            }
        });
    }

    private byte[] readWebp(MultipartFile file, String field, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(field, "La imagen es obligatoria");
        }
        if (file.getSize() > maxBytes) {
            throw new BusinessException(field, "La imagen pesa demasiado (máximo " + (maxBytes / 1024) + " KB)");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(field, "No pudimos leer la imagen");
        }
        if (!isWebp(bytes)) {
            throw new BusinessException(field, "La imagen debe estar en formato WebP");
        }
        return bytes;
    }

    // Un WebP empieza con "RIFF", 4 bytes de tamaño y "WEBP": se mira el contenido, no el nombre ni el tipo declarado.
    static boolean isWebp(byte[] bytes) {
        return bytes.length > 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id", "El producto indicado no existe"));
    }

    private ProductImage findImage(UUID productId, UUID imageId) {
        return imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new NotFoundException("imageId", "La imagen indicada no existe"));
    }
}
