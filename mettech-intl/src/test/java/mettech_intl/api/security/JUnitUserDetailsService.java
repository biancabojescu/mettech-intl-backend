package mettech_intl.api.security;

import mettech_intl.model.Users;
import mettech_intl.model.dao.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@Primary
public class JUnitUserDetailsService implements UserDetailsService {
    @Autowired
    private UsersRepository localUserDAO;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> opUser = localUserDAO.findByUsernameIgnoreCase(username);
        if (opUser.isPresent())
            return opUser.get();
        return null;
    }

}