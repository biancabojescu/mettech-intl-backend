package mettech_intl.service;

import mettech_intl.model.Orders;
import mettech_intl.model.OrdersQuantities;
import mettech_intl.model.Product;
import mettech_intl.model.Users;
import mettech_intl.model.dao.OrdersRepository;
import mettech_intl.model.dao.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrdersRepository ordersRepository;
    private final ProductRepository productRepository;

    public OrderService(OrdersRepository ordersRepository, ProductRepository productRepository) {
        this.ordersRepository = ordersRepository;
        this.productRepository = productRepository;
    }

    public List<Orders> getOrders(Users user) {
        return ordersRepository.findByUser(user);
    }

    @Transactional
    public Orders addProductToOrder(Users user, Product product) {
        Orders order = getCurrentOrder(user);

        Optional<OrdersQuantities> optionalQuantity = order.getQuantities().stream()
                .filter(q -> q.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (optionalQuantity.isPresent()) {
            OrdersQuantities quantity = optionalQuantity.get();
            quantity.setQuantity(quantity.getQuantity() + 1);
        } else {
            OrdersQuantities quantity = new OrdersQuantities();
            quantity.setProduct(product);
            quantity.setQuantity(1);
            quantity.setOrder(order);
            order.getQuantities().add(quantity);
        }

        return ordersRepository.save(order);
    }

    @Transactional
    public Orders updateProductQuantity(Users user, Product product, int quantity) {
        Orders order = getCurrentOrder(user);

        Optional<OrdersQuantities> optionalQuantity = order.getQuantities().stream()
                .filter(q -> q.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (optionalQuantity.isPresent()) {
            OrdersQuantities ordersQuantity = optionalQuantity.get();
            ordersQuantity.setQuantity(quantity);
            return ordersRepository.save(order);
        } else {
            throw new RuntimeException("Product not found in order");
        }
    }

    @Transactional
    public Orders removeProductFromOrder(Users user, Product product) {
        Orders order = getCurrentOrder(user);

        Optional<OrdersQuantities> optionalQuantity = order.getQuantities().stream()
                .filter(q -> q.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (optionalQuantity.isPresent()) {
            OrdersQuantities ordersQuantity = optionalQuantity.get();
            order.getQuantities().remove(ordersQuantity);
            return ordersRepository.save(order);
        } else {
            throw new RuntimeException("Product not found in order");
        }
    }

    private Orders getCurrentOrder(Users user) {
        return ordersRepository.findByUserAndStatus(user, "CURRENT")
                .orElseGet(() -> createNewOrder(user));
    }

    private Orders createNewOrder(Users user) {
        Orders order = new Orders();
        order.setUser(user);
        order.setStatus("CURRENT");

        return ordersRepository.save(order);
    }

    public void clearCart(Users user) {
    }
}
