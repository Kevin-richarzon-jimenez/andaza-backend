package com.andanza.backend.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CalculatedItem> items,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shipping,
        BigDecimal total
) {
    public record CalculatedItem(
            String productId,
            String size,
            String color,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal itemSubtotal
    ) {
    }
}
