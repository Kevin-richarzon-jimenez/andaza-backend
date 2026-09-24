package com.andanza.backend.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        List<CalculatedItem> items,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shipping,
        BigDecimal total
) {

    public record CalculatedItem(
            UUID variantId,
            UUID productId,
            String productName,
            String color,
            String size,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal itemSubtotal
    ) {
    }
}
