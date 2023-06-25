package mettech_intl.model.dao;

import mettech_intl.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsernameIgnoreCase(String username);
    Optional<Users> findByEmailIgnoreCase(String email);
}
