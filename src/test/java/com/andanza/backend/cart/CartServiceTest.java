package com.andanza.backend.cart;

import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductVariant;
import com.andanza.backend.catalog.ProductVariantRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private CartService cartService;

    private ProductVariant variantWith(String price, int stock) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Runner Air");
        product.setPrice(new BigDecimal(price));
        ProductVariant variant = new ProductVariant();
        variant.setId(UUID.randomUUID());
        variant.setProduct(product);
        variant.setColor("Negro");
        variant.setSize("40");
        variant.setStock(stock);
        when(variantRepository.findById(variant.getId())).thenReturn(Optional.of(variant));
        return variant;
    }

    private CartCalculationRequest request(String discountCode, CartItemRequest... items) {
        return new CartCalculationRequest(List.of(items), discountCode);
    }

    @Test
    void chargesShippingWhenTheOrderIsBelowTheFreeShippingThreshold() {
        ProductVariant variant = variantWith("100000", 5);

        CartResponse response = cartService.calculate(request(null, new CartItemRequest(variant.getId(), 1)));

        assertThat(response.subtotal()).isEqualByComparingTo("100000");
        assertThat(response.shipping()).isEqualByComparingTo("12000");
        assertThat(response.total()).isEqualByComparingTo("112000");
    }

    @Test
    void shippingIsFreeFromTheThreshold() {
        ProductVariant variant = variantWith("100000", 5);

        CartResponse response = cartService.calculate(request(null, new CartItemRequest(variant.getId(), 2)));

        assertThat(response.shipping()).isEqualByComparingTo("0");
        assertThat(response.total()).isEqualByComparingTo("200000");
    }

    @Test
    void appliesTheCouponPercentageBeforeDecidingShipping() {
        ProductVariant variant = variantWith("100000", 5);

        CartResponse response = cartService.calculate(request("andanza10", new CartItemRequest(variant.getId(), 2)));

        assertThat(response.discount()).isEqualByComparingTo("20000");
        assertThat(response.total()).isEqualByComparingTo("180000");
    }

    @Test
    void rejectsAnUnknownCoupon() {
        ProductVariant variant = variantWith("100000", 5);

        assertThatThrownBy(() -> cartService.calculate(request("NOPE", new CartItemRequest(variant.getId(), 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cupón");
    }

    @Test
    void rejectsAQuantityAboveTheStock() {
        ProductVariant variant = variantWith("100000", 3);

        assertThatThrownBy(() -> cartService.calculate(request(null, new CartItemRequest(variant.getId(), 4))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo quedan 3");
    }

    @Test
    void reportsASoldOutVariantAsSuch() {
        ProductVariant variant = variantWith("100000", 0);

        assertThatThrownBy(() -> cartService.calculate(request(null, new CartItemRequest(variant.getId(), 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("agotado");
    }

    @Test
    void addsUpRepeatedLinesOfTheSameVariantBeforeCheckingTheStock() {
        ProductVariant variant = variantWith("100000", 5);

        assertThatThrownBy(() -> cartService.calculate(request(null,
                new CartItemRequest(variant.getId(), 3), new CartItemRequest(variant.getId(), 3))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo quedan 5");
    }

    @Test
    void rejectsAVariantThatDoesNotExist() {
        UUID unknown = UUID.randomUUID();
        when(variantRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.calculate(request(null, new CartItemRequest(unknown, 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no existe");
    }
}
