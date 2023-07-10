package mettech_intl.model.dao;

import mettech_intl.model.Users;
import mettech_intl.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUser(Users user);
    List<VerificationToken> findByUser_IdOrderByIdDesc(Long id);
}
