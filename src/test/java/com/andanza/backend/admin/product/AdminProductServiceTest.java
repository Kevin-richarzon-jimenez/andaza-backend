package com.andanza.backend.admin.product;

import com.andanza.backend.catalog.Category;
import com.andanza.backend.catalog.CategoryRepository;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.catalog.ProductResponse;
import com.andanza.backend.catalog.ProductVariantResponse;
import com.andanza.backend.exception.BusinessException;
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

    @InjectMocks
    private AdminProductService adminProductService;

    private final UUID categoryId = UUID.randomUUID();

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
}
