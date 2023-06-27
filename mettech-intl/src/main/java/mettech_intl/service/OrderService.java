package mettech_intl.service;

import mettech_intl.model.Orders;
import mettech_intl.model.Users;
import mettech_intl.model.dao.OrdersRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private OrdersRepository ordersRepository;

    public OrderService(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    public List<Orders> getOrders(Users user){
        return ordersRepository.findByUser(user);
    }

}
