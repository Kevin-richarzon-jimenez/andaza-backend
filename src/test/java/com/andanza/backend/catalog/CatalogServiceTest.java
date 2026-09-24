package com.andanza.backend.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void filterOptionsSortNumericSizesAsNumbersAndLettersLast() {
        when(variantRepository.findDistinctSizes()).thenReturn(List.of("40", "M", "9", "37.5", "38", "S"));
        when(variantRepository.findDistinctColors()).thenReturn(List.of("Blanco", "Negro"));
        when(productRepository.findMinPrice()).thenReturn(new BigDecimal("129900"));
        when(productRepository.findMaxPrice()).thenReturn(new BigDecimal("329900"));

        CatalogFilterOptionsResponse options = catalogService.getFilterOptions();

        assertThat(options.sizes()).containsExactly("9", "37.5", "38", "40", "M", "S");
        assertThat(options.colors()).containsExactly("Blanco", "Negro");
        assertThat(options.minPrice()).isEqualByComparingTo("129900");
        assertThat(options.maxPrice()).isEqualByComparingTo("329900");
    }

    @Test
    void filterOptionsOfAnEmptyCatalogHaveNoValuesAndNoPrices() {
        when(variantRepository.findDistinctSizes()).thenReturn(List.of());
        when(variantRepository.findDistinctColors()).thenReturn(List.of());

        CatalogFilterOptionsResponse options = catalogService.getFilterOptions();

        assertThat(options.sizes()).isEmpty();
        assertThat(options.colors()).isEmpty();
        assertThat(options.minPrice()).isNull();
        assertThat(options.maxPrice()).isNull();
    }
}
