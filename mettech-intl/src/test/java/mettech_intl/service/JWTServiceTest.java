package mettech_intl.service;

import mettech_intl.model.Users;
import mettech_intl.model.dao.UsersRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JWTServiceTest {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UsersRepository usersRepository;

    @Test
    public  void testVerificationTokenNotUsableForLogin(){
        Users user = usersRepository.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generateVerificationJWT(user);
        Assertions.assertNull(jwtService.getUsername(token),"Verification token should not contain username!");
    }

    @Test
    public void TestAuthTokenReturnUsername(){
        Users user = usersRepository.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generateJWT(user);
        Assertions.assertEquals(user.getUsername(), jwtService.getUsername(token),"Token for auth should contain users username.");
    }

}
