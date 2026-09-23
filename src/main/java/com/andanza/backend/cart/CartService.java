package com.andanza.backend.cart;

import com.andanza.backend.catalog.CatalogService;
import com.andanza.backend.catalog.Product;
import com.andanza.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Diseño sin BD ni sesión de servidor: el cliente (front-end) envía el
// estado completo del carrito en cada petición (todos sus items) y este
// servicio solo VALIDA y CALCULA (subtotal, descuento, envío, total). No
// hay "agregar/quitar" en el servidor porque no hay dónde persistirlo
// todavía; el front-end mantiene la lista y este endpoint la recalcula.
// TODO (BD): cuando exista BD y sesión de usuario, mover el carrito a una
// tabla "cart_item" asociada al usuario.
@Service
public class CartService {

    private static final BigDecimal SHIPPING_COST = new BigDecimal("12000");
    private static final BigDecimal FREE_SHIPPING_FROM = new BigDecimal("150000");

    // Cupones de ejemplo. TODO (BD): mover a una tabla "coupon" con vigencia, etc.
    private static final Map<String, BigDecimal> DEMO_COUPONS = Map.of(
            "ANDANZA10", new BigDecimal("0.10"),
            "BIENVENIDA15", new BigDecimal("0.15")
    );

    private final CatalogService catalogService;

    public CartService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public CartResponse calculate(CartCalculationRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartResponse.CalculatedItem> calculatedItems = new ArrayList<>();

        for (CartItemRequest item : request.items()) {
            // El precio nunca lo manda el cliente -- se busca en el catálogo real,
            // así nadie puede manipular el total mandando un precio propio.
            Product product = catalogService.findById(item.productId());
            catalogService.validateVariant(product, item.color(), item.size());

            BigDecimal itemSubtotal = product.price()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemSubtotal);
            calculatedItems.add(new CartResponse.CalculatedItem(
                    item.productId(), item.size(), item.color(),
                    item.quantity(), product.price(), itemSubtotal));
        }

        BigDecimal discount = calculateDiscount(request.discountCode(), subtotal);
        BigDecimal baseWithDiscount = subtotal.subtract(discount);
        BigDecimal shipping = baseWithDiscount.compareTo(FREE_SHIPPING_FROM) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_COST;
        BigDecimal total = baseWithDiscount.add(shipping);

        return new CartResponse(calculatedItems, subtotal, discount, shipping, total);
    }

    private BigDecimal calculateDiscount(String discountCode, BigDecimal subtotal) {
        if (discountCode == null || discountCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal percentage = DEMO_COUPONS.get(discountCode.toUpperCase());
        if (percentage == null) {
            throw new BusinessException("discountCode", "El cupón ingresado no es válido o ya expiró");
        }
        return subtotal.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
    }
}
