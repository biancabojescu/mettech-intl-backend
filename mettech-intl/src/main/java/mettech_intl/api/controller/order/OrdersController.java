package mettech_intl.api.controller.order;

import mettech_intl.model.Orders;
import mettech_intl.model.Users;
import mettech_intl.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    private OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Orders> getOrders(@AuthenticationPrincipal Users user){
        return orderService.getOrders(user);
    }
}
