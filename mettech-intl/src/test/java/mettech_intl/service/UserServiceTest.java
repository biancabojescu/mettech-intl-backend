package mettech_intl.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import mettech_intl.api.models.LoginBody;
import mettech_intl.api.models.PasswordResetBody;
import mettech_intl.api.models.RegistrationBody;
import mettech_intl.exception.EmailFailureException;
import mettech_intl.exception.EmailNotFoundException;
import mettech_intl.exception.UserAlreadyExistsException;
import mettech_intl.exception.UserNotVerifiedException;
import mettech_intl.model.Users;
import mettech_intl.model.VerificationToken;
import mettech_intl.model.dao.UsersRepository;
import mettech_intl.model.dao.VerificationTokenRepository;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {
    @RegisterExtension
    private static GreenMailExtension greenMailExtension= new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig()
                    .withUser("springboot","secret")).withPerMethodLifecycle(true);
    @Autowired
    private UserService userService;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private EncryptionService encryptionService;

    @Test
    @Transactional
    public void testRegisterUser() throws MessagingException {
        RegistrationBody body = new RegistrationBody();

        body.setUsername("UserA");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setPassword("MySecretPassword123");
        Assertions.assertThrows(UserAlreadyExistsException.class, ()-> userService.registerUser(body), "Username should already be in use.\n");

        body.setUsername("UserServiceTest$testRegisterUser");
        body.setEmail("userA@junit.com");
        Assertions.assertThrows(UserAlreadyExistsException.class, ()-> userService.registerUser(body), "Email should already be in use.\n");

        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        Assertions.assertDoesNotThrow(() -> userService.registerUser(body),
                "User should register successfully.");

        Assertions.assertEquals(body.getEmail(), greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString());

    }

    @Test
    @Transactional
    public void testLoginUser() throws UserNotVerifiedException, EmailFailureException {
        LoginBody body = new LoginBody();
        body.setPassword("UserA-NotExists");
        body.setPassword("PasswordA123-BadPass");
        Assertions.assertNull(userService.loginUser(body), "The user should not exists.");
        body.setUsername("UserA");
        Assertions.assertNull(userService.loginUser(body), "The password shoul be incorect.");
        body.setPassword("PasswordA123");
        Assertions.assertNotNull(userService.loginUser(body),"The user should login successfully.");
        body.setUsername("UserB");
        body.setPassword("PasswordB123");
        try{
            userService.loginUser(body);
            Assertions.assertTrue(false,"User should not have email verified.");
        } catch(UserNotVerifiedException ex){
            Assertions.assertTrue(ex.isNewEmailSent(),"Email verification should be sent.");
            Assertions.assertEquals(1,greenMailExtension.getReceivedMessages().length);
        }
        try{
            userService.loginUser(body);
            Assertions.assertTrue(false,"User should not have email verified.");
        } catch(UserNotVerifiedException ex){
            Assertions.assertFalse(ex.isNewEmailSent(),"Email verification should not be resent.");
            Assertions.assertEquals(1,greenMailExtension.getReceivedMessages().length);
        }
    }

    @Test
    @Transactional
    public void testVerifyUser() throws EmailFailureException{
        Assertions.assertFalse(userService.verifyUser("Bad token"), "Token that is bad or does not exists should return false.");
        LoginBody body = new LoginBody();
        body.setUsername("UserB");
        body.setPassword("PasswordB123");
        try{
            userService.loginUser(body);
            Assertions.assertTrue(false,"User should not have email verified.");
        } catch(UserNotVerifiedException ex){
            List<VerificationToken> tokens = verificationTokenRepository.findByUser_IdOrderByIdDesc(2L);
            String token = tokens.get(0).getToken();
            Assertions.assertTrue(userService.verifyUser(token));
            Assertions.assertNotNull(body, "The user should now be verified.");
        }
    }

    @Test
    @Transactional
    public void testForgotPassword() throws MessagingException {
        Assertions.assertThrows(EmailNotFoundException.class, ()->userService.forgotPassword("UserNotExist@junit.com"));
        Assertions.assertDoesNotThrow(()->userService.forgotPassword("UserA@junit.com"), "Non existing email should be rejected.");
        Assertions.assertEquals("UserA@junit.com", greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString(), "Password reset email should be sent.");
    }

    @Test
    @Transactional
    public void testResetPassword(){
        Users user = usersRepository.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generatePasswordResetJWT(user);
        PasswordResetBody body = new PasswordResetBody();
        body.setToken(token);
        body.setPassword("parolaNoua123");
        userService.resetPassword(body);
        user = usersRepository.findByUsernameIgnoreCase("UserA").get();
        Assertions.assertTrue(encryptionService.verifyPassword("parolaNoua123",user.getPassword()),"Password change should be written to DB.");
    }


}
