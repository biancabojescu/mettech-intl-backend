package mettech_intl.model.dao;

import mettech_intl.model.Orders;
import mettech_intl.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUser(Users user);
    Optional<Orders> findByUserAndStatus(Users user, String status);
}
