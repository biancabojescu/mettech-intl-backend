package mettech_intl.api.controller.order;

import mettech_intl.model.Orders;
import mettech_intl.model.Product;
import mettech_intl.model.Users;
import mettech_intl.model.dao.ProductRepository;
import mettech_intl.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;
    private final ProductRepository productRepository;

    public OrdersController(OrderService orderService, ProductRepository productRepository) {
        this.orderService = orderService;
        this.productRepository = productRepository;
    }

    @GetMapping("/current")
    public List<Orders> getOrders(@AuthenticationPrincipal Users user) {
        return orderService.getOrders(user);
    }

    @PostMapping("/add")
    public Orders addToCart(@AuthenticationPrincipal Users user, @RequestBody Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        return orderService.addProductToOrder(user, product);
    }

    @PatchMapping("/update")
    public Orders updateCartItem(@AuthenticationPrincipal Users user, @RequestBody OrderUpdateRequest updateRequest) {
        Product product = productRepository.findById(updateRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));
        return orderService.updateProductQuantity(user, product, updateRequest.getQuantity());
    }

    @DeleteMapping("/remove/{productId}")
    public Orders removeFromCart(@AuthenticationPrincipal Users user, @PathVariable Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        return orderService.removeProductFromOrder(user, product);
    }

    @DeleteMapping("/clear")
    public void clearCart(@AuthenticationPrincipal Users user) {
        orderService.clearCart(user);
    }
}

class OrderUpdateRequest {
    private Long productId;
    private int quantity;

    // Getters and setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
