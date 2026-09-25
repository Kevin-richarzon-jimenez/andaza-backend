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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    private static final String BASE = "https://x.supabase.co/storage/v1/object/public/product-images/";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository imageRepository;

    @Mock
    private SupabaseStorage storage;

    @InjectMocks
    private ProductImageService imageService;

    private final UUID productId = UUID.randomUUID();
    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(productId);
        product.addVariant(variant("Negro", "40"));
        product.addVariant(variant("Café", "40"));
    }

    private static ProductVariant variant(String color, String size) {
        ProductVariant variant = new ProductVariant();
        variant.setColor(color);
        variant.setSize(size);
        variant.setStock(5);
        return variant;
    }

    private static byte[] webpBytes(int length) {
        byte[] bytes = new byte[length];
        System.arraycopy("RIFF".getBytes(), 0, bytes, 0, 4);
        System.arraycopy("WEBP".getBytes(), 0, bytes, 8, 4);
        return bytes;
    }

    private static MockMultipartFile file(String name, byte[] content) {
        return new MockMultipartFile(name, name + ".webp", "image/webp", content);
    }

    private ProductImage stored(String color, int sortOrder) {
        ProductImage image = new ProductImage();
        image.setId(UUID.randomUUID());
        image.setProduct(product);
        image.setColor(color);
        image.setSortOrder(sortOrder);
        image.setUrl(BASE + "products/" + productId + "/" + image.getId() + ".webp");
        image.setThumbnailUrl(BASE + "products/" + productId + "/" + image.getId() + "-thumb.webp");
        return image;
    }

    private void productExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    }

    private void noImagesYet() {
        when(imageRepository.findByProductIdAndColorIgnoreCaseOrderBySortOrderAscIdAsc(eq(productId), anyString()))
                .thenReturn(List.of());
    }

    @Test
    void addsAnImageToAColorTheProductHas() {
        productExists();
        noImagesYet();
        when(storage.publicUrl(anyString())).thenAnswer(call -> BASE + call.getArgument(0));
        when(imageRepository.saveAndFlush(any(ProductImage.class))).thenAnswer(call -> call.getArgument(0));

        ProductImageResponse response = imageService.add(productId, " negro ", file("image", webpBytes(2000)), file("thumbnail", webpBytes(500)));

        assertThat(response.color()).isEqualTo("Negro");
        assertThat(response.sortOrder()).isZero();
        assertThat(response.url()).startsWith(BASE + "products/" + productId + "/").endsWith(".webp");
        assertThat(response.thumbnailUrl()).endsWith("-thumb.webp");
        verify(storage, org.mockito.Mockito.times(2)).upload(anyString(), any(byte[].class), eq("image/webp"));
    }

    @Test
    void placesANewImageAfterTheOnesTheColorAlreadyHas() {
        productExists();
        when(imageRepository.findByProductIdAndColorIgnoreCaseOrderBySortOrderAscIdAsc(eq(productId), eq("Café")))
                .thenReturn(List.of(stored("Café", 0), stored("Café", 1)));
        when(storage.publicUrl(anyString())).thenAnswer(call -> BASE + call.getArgument(0));
        when(imageRepository.saveAndFlush(any(ProductImage.class))).thenAnswer(call -> call.getArgument(0));

        ProductImageResponse response = imageService.add(productId, "Café", file("image", webpBytes(2000)), file("thumbnail", webpBytes(500)));

        assertThat(response.sortOrder()).isEqualTo(2);
    }

    @Test
    void rejectsAColorTheProductDoesNotHave() {
        productExists();

        assertThatThrownBy(() -> imageService.add(productId, "Rojo", file("image", webpBytes(2000)), file("thumbnail", webpBytes(500))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Rojo");
        verify(storage, never()).upload(anyString(), any(byte[].class), anyString());
    }

    @Test
    void rejectsAMissingProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imageService.add(productId, "Negro", file("image", webpBytes(2000)), file("thumbnail", webpBytes(500))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsMoreImagesThanTheColorAllows() {
        productExists();
        List<ProductImage> full = new ArrayList<>();
        for (int i = 0; i < ProductImageService.MAX_IMAGES_PER_COLOR; i++) {
            full.add(stored("Negro", i));
        }
        when(imageRepository.findByProductIdAndColorIgnoreCaseOrderBySortOrderAscIdAsc(eq(productId), eq("Negro"))).thenReturn(full);

        assertThatThrownBy(() -> imageService.add(productId, "Negro", file("image", webpBytes(2000)), file("thumbnail", webpBytes(500))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("hasta 5");
        verify(storage, never()).upload(anyString(), any(byte[].class), anyString());
    }

    @Test
    void rejectsAFileThatIsNotWebpEvenIfItClaimsToBe() {
        productExists();
        noImagesYet();
        byte[] png = new byte[100];
        System.arraycopy(new byte[] {(byte) 0x89, 'P', 'N', 'G'}, 0, png, 0, 4);

        assertThatThrownBy(() -> imageService.add(productId, "Negro", file("image", png), file("thumbnail", webpBytes(500))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("WebP");
        verify(storage, never()).upload(anyString(), any(byte[].class), anyString());
    }

    @Test
    void rejectsAnImageThatIsTooBig() {
        productExists();
        noImagesYet();

        assertThatThrownBy(() -> imageService.add(productId, "Negro",
                file("image", webpBytes((int) ProductImageService.MAX_IMAGE_BYTES + 1)), file("thumbnail", webpBytes(500))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pesa demasiado");
    }

    @Test
    void rejectsAnEmptyThumbnail() {
        productExists();
        noImagesYet();

        assertThatThrownBy(() -> imageService.add(productId, "Negro", file("image", webpBytes(2000)), file("thumbnail", new byte[0])))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cleansUpTheUploadedFilesIfSavingTheRowFails() {
        productExists();
        noImagesYet();
        when(storage.publicUrl(anyString())).thenAnswer(call -> BASE + call.getArgument(0));
        when(imageRepository.saveAndFlush(any(ProductImage.class))).thenThrow(new IllegalStateException("db caída"));

        assertThatThrownBy(() -> imageService.add(productId, "Negro", file("image", webpBytes(2000)), file("thumbnail", webpBytes(500))))
                .isInstanceOf(IllegalStateException.class);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> deleted = ArgumentCaptor.forClass(List.class);
        verify(storage).deleteQuietly(deleted.capture());
        assertThat(deleted.getValue()).hasSize(2);
    }

    @Test
    void doesNotRegisterAnImageWhenTheStorageFails() {
        productExists();
        noImagesYet();
        when(storage.publicUrl(anyString())).thenAnswer(call -> BASE + call.getArgument(0));
        doThrow(new BusinessException("image", "sin almacenamiento")).when(storage).upload(anyString(), any(byte[].class), anyString());

        assertThatThrownBy(() -> imageService.add(productId, "Negro", file("image", webpBytes(2000)), file("thumbnail", webpBytes(500))))
                .isInstanceOf(BusinessException.class);
        verify(imageRepository, never()).saveAndFlush(any(ProductImage.class));
    }

    @Test
    void removesAnImageAndItsFilesOfTheProduct() {
        productExists();
        ProductImage image = stored("Negro", 0);
        when(imageRepository.findByIdAndProductId(image.getId(), productId)).thenReturn(Optional.of(image));
        when(storage.pathOf(image.getUrl())).thenReturn("products/a.webp");
        when(storage.pathOf(image.getThumbnailUrl())).thenReturn("products/a-thumb.webp");

        imageService.remove(productId, image.getId());

        verify(imageRepository).delete(image);
        verify(storage).deleteQuietly(List.of("products/a.webp", "products/a-thumb.webp"));
    }

    @Test
    void anImageOfAnotherProductIsNotFound() {
        productExists();
        UUID imageId = UUID.randomUUID();
        when(imageRepository.findByIdAndProductId(imageId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imageService.remove(productId, imageId)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> imageService.makeCover(productId, imageId)).isInstanceOf(NotFoundException.class);
        verify(imageRepository, never()).delete(any(ProductImage.class));
    }

    @Test
    void makingAnImageTheCoverMovesItFirstAndKeepsTheRestInOrder() {
        productExists();
        ProductImage first = stored("Negro", 0);
        ProductImage second = stored("Negro", 1);
        ProductImage third = stored("Negro", 2);
        when(imageRepository.findByIdAndProductId(third.getId(), productId)).thenReturn(Optional.of(third));
        when(imageRepository.findByProductIdAndColorIgnoreCaseOrderBySortOrderAscIdAsc(productId, "Negro"))
                .thenReturn(List.of(first, second, third));

        List<ProductImageResponse> result = imageService.makeCover(productId, third.getId());

        assertThat(result).extracting(ProductImageResponse::id).containsExactly(third.getId(), first.getId(), second.getId());
        assertThat(third.getSortOrder()).isZero();
        assertThat(first.getSortOrder()).isEqualTo(1);
        assertThat(second.getSortOrder()).isEqualTo(2);
    }

    @Test
    void recognizesWebpByItsContent() {
        assertThat(ProductImageService.isWebp(webpBytes(64))).isTrue();
        assertThat(ProductImageService.isWebp(new byte[64])).isFalse();
        assertThat(ProductImageService.isWebp(new byte[5])).isFalse();
    }
}
