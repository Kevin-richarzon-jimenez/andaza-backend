package com.andanza.backend.cart;

import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductVariant;
import com.andanza.backend.catalog.ProductVariantRepository;
import com.andanza.backend.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Diseño sin sesión de servidor: el cliente envía el carrito completo en cada petición y este
// servicio valida y calcula (subtotal, descuento, envío, total). Precio y stock salen siempre de
// la base de datos: nunca se confía en lo que mande el cliente.
@Service
public class CartService {

    private static final BigDecimal SHIPPING_COST = new BigDecimal("12000");
    private static final BigDecimal FREE_SHIPPING_FROM = new BigDecimal("150000");

    // Cupones de ejemplo. TODO: mover a una tabla "coupons" con vigencia cuando el negocio defina cómo funcionan.
    private static final Map<String, BigDecimal> DEMO_COUPONS = Map.of(
            "ANDANZA10", new BigDecimal("0.10"),
            "BIENVENIDA15", new BigDecimal("0.15")
    );

    private final ProductVariantRepository variantRepository;

    public CartService(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse calculate(CartCalculationRequest request) {
        // Si la misma variante llega en dos líneas, se suman las cantidades antes de comprobar el stock.
        Map<UUID, Integer> quantities = new LinkedHashMap<>();
        for (CartItemRequest item : request.items()) {
            quantities.merge(item.variantId(), item.quantity(), Integer::sum);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartResponse.CalculatedItem> calculatedItems = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : quantities.entrySet()) {
            ProductVariant variant = variantRepository.findById(entry.getKey())
                    .orElseThrow(() -> new BusinessException("variantId", "La variante indicada no existe"));
            Product product = variant.getProduct();
            int quantity = entry.getValue();
            checkStock(product, variant, quantity);

            BigDecimal itemSubtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemSubtotal);
            calculatedItems.add(new CartResponse.CalculatedItem(
                    variant.getId(), product.getId(), product.getName(), variant.getColor(), variant.getSize(),
                    quantity, product.getPrice(), itemSubtotal));
        }

        BigDecimal discount = calculateDiscount(request.discountCode(), subtotal);
        BigDecimal baseWithDiscount = subtotal.subtract(discount);
        BigDecimal shipping = baseWithDiscount.compareTo(FREE_SHIPPING_FROM) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_COST;
        BigDecimal total = baseWithDiscount.add(shipping);

        return new CartResponse(calculatedItems, subtotal, discount, shipping, total);
    }

    private void checkStock(Product product, ProductVariant variant, int quantity) {
        if (quantity <= variant.getStock()) {
            return;
        }
        String label = product.getName() + " (" + variant.getColor() + ", talla " + variant.getSize() + ")";
        String message = variant.getStock() == 0
                ? label + " está agotado"
                : "Solo quedan " + variant.getStock() + " unidades de " + label;
        throw new BusinessException("quantity", message);
    }

    private BigDecimal calculateDiscount(String discountCode, BigDecimal subtotal) {
        if (discountCode == null || discountCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal percentage = DEMO_COUPONS.get(discountCode.trim().toUpperCase());
        if (percentage == null) {
            throw new BusinessException("discountCode", "El cupón ingresado no es válido o ya expiró");
        }
        return subtotal.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
    }
}
