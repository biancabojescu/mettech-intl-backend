package mettech_intl.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import mettech_intl.model.Users;
import mettech_intl.model.dao.UsersRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
public class JWTServiceTest {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UsersRepository usersRepository;
    @Value("${jwt.algorithm.key}")
    private String algorithmKey;

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

    @Test
    public void testLoginJWTNotGeneratedByUs(){
        String token = JWT.create().withClaim("USERNAME", "UserA").sign(Algorithm.HMAC256("NotTheRealSecret"));
        Assertions.assertThrows(SignatureVerificationException.class,
                ()->jwtService.getUsername(token));
    }

    @Test
    public void testLoginJWTCorrectSignedNoIssuer(){
        String token = JWT.create().withClaim("USERNAME","UserA").sign(Algorithm.HMAC256(algorithmKey));
        Assertions.assertThrows(MissingClaimException.class,
                ()->jwtService.getUsername(token));
    }

    @Test
    public void testPasswordResetToken(){
        Users user = usersRepository.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generatePasswordResetJWT(user);
        Assertions.assertEquals(user.getEmail(), jwtService.getResetPasswordEmail(token), "Email should match inside JWT.");
    }

    @Test
    public void testResetPasswordJWTNotGeneratedByUs(){
        String token = JWT.create().withClaim("RESET_PASSWORD_EMAIL", "UserA@junit.com").sign(Algorithm.HMAC256("NotTheRealSecret"));
        Assertions.assertThrows(SignatureVerificationException.class,
                ()->jwtService.getResetPasswordEmail(token));
    }

    @Test
    public void testResetPasswordJWTCorrectSignedNoIssuer(){
        String token = JWT.create().withClaim("RESET_PASSWORD_EMAIL","UserA@junit.com").sign(Algorithm.HMAC256(algorithmKey));
        Assertions.assertThrows(MissingClaimException.class,
                ()->jwtService.getResetPasswordEmail(token));
    }

}
