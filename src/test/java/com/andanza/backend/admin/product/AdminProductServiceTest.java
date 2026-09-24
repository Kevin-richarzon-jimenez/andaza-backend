package com.andanza.backend.admin.product;

import com.andanza.backend.catalog.Category;
import com.andanza.backend.catalog.CategoryRepository;
import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.catalog.ProductResponse;
import com.andanza.backend.catalog.ProductVariantRepository;
import com.andanza.backend.catalog.ProductVariantResponse;
import com.andanza.backend.exception.BusinessException;
import com.andanza.backend.exception.ConflictException;
import com.andanza.backend.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private AdminProductService adminProductService;

    private final UUID categoryId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    private Product existingProduct() {
        Category oldCategory = new Category();
        oldCategory.setId(UUID.randomUUID());
        oldCategory.setName("Deportivo");
        Product product = new Product();
        product.setId(productId);
        product.setName("Runner Air");
        product.setBrand("Andanza");
        product.setDescription("Tenis ligeros para correr");
        product.setPrice(new BigDecimal("289900"));
        product.setCategory(oldCategory);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        return product;
    }

    private AdminProductUpdateRequest updateRequest() {
        return new AdminProductUpdateRequest("  Runner Air 2 ", "Andanza", categoryId, new BigDecimal("299900"),
                "Tenis ligeros con nueva suela");
    }

    private AdminProductRequest request(ProductVariantRequest... variants) {
        return new AdminProductRequest("Bota QA", "Andanza", categoryId, new BigDecimal("199900"),
                "Bota de prueba de cuero", List.of(variants));
    }

    private void categoryExists() {
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Botas");
        category.setDescription("Botas y botines");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    }

    @Test
    void createsExactlyTheRequestedVariants() {
        categoryExists();
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = adminProductService.create(request(
                new ProductVariantRequest("Negro", "38", 3),
                new ProductVariantRequest("Café", "42", 2)));

        assertThat(response.variants())
                .extracting(ProductVariantResponse::color, ProductVariantResponse::size, ProductVariantResponse::stock)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Negro", "38", 3),
                        org.assertj.core.groups.Tuple.tuple("Café", "42", 2));
    }

    @Test
    void rejectsTheSameColorAndSizeTwiceIgnoringCase() {
        categoryExists();

        assertThatThrownBy(() -> adminProductService.create(request(
                new ProductVariantRequest("Negro", "38", 1),
                new ProductVariantRequest("negro", "38", 2))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("repetida");
        verify(productRepository, never()).save(any());
    }

    @Test
    void rejectsACategoryThatDoesNotExist() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.create(request(new ProductVariantRequest("Negro", "38", 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("categoría");
        verify(productRepository, never()).save(any());
    }

    @Test
    void updatesTheProductDataAndTrimsTheName() {
        existingProduct();
        categoryExists();

        ProductResponse response = adminProductService.update(productId, updateRequest());

        assertThat(response.name()).isEqualTo("Runner Air 2");
        assertThat(response.price()).isEqualByComparingTo("299900");
        assertThat(response.category().name()).isEqualTo("Botas");
    }

    @Test
    void updatingAMissingProductIsNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.update(productId, updateRequest()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updatingWithACategoryThatDoesNotExistIsABusinessRuleError() {
        existingProduct();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.update(productId, updateRequest()))
                .isInstanceOf(BusinessException.class)
                .isNotInstanceOf(NotFoundException.class)
                .hasMessageContaining("categoría");
    }

    @Test
    void deletesAnExistingProduct() {
        Product product = existingProduct();

        adminProductService.delete(productId);

        verify(productRepository).delete(product);
    }

    @Test
    void deletingAMissingProductIsNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.delete(productId)).isInstanceOf(NotFoundException.class);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void addsAVariantToAnExistingProduct() {
        existingProduct();
        when(variantRepository.existsByProductIdAndColorIgnoreCaseAndSize(productId, "Negro", "40")).thenReturn(false);
        when(variantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariantResponse response = adminProductService.addVariant(productId, new ProductVariantRequest(" Negro ", " 40 ", 5));

        assertThat(response.color()).isEqualTo("Negro");
        assertThat(response.size()).isEqualTo("40");
        assertThat(response.stock()).isEqualTo(5);
    }

    @Test
    void rejectsAVariantThatAlreadyExistsInTheProduct() {
        existingProduct();
        when(variantRepository.existsByProductIdAndColorIgnoreCaseAndSize(productId, "Negro", "40")).thenReturn(true);

        assertThatThrownBy(() -> adminProductService.addVariant(productId, new ProductVariantRequest("Negro", "40", 5)))
                .isInstanceOf(ConflictException.class);
        verify(variantRepository, never()).save(any());
    }

    @Test
    void addingAVariantToAMissingProductIsNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.addVariant(productId, new ProductVariantRequest("Negro", "40", 5)))
                .isInstanceOf(NotFoundException.class);
    }
}
