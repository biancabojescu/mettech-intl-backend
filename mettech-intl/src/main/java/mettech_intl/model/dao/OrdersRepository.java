package mettech_intl.model.dao;

import mettech_intl.model.Orders;
import mettech_intl.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUser(Users user);

}
