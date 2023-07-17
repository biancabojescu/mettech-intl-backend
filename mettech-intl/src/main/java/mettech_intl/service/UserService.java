package mettech_intl.service;

import jakarta.transaction.Transactional;
import mettech_intl.api.models.LoginBody;
import mettech_intl.api.models.PasswordResetBody;
import mettech_intl.api.models.RegistrationBody;
import mettech_intl.exception.EmailFailureException;
import mettech_intl.exception.EmailNotFoundException;
import mettech_intl.exception.UserNotVerifiedException;
import mettech_intl.model.VerificationToken;
import mettech_intl.model.dao.UsersRepository;
import mettech_intl.exception.UserAlreadyExistsException;
import mettech_intl.model.Users;
import mettech_intl.model.dao.VerificationTokenRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UsersRepository usersRepository;
    private VerificationTokenRepository verificationTokenRepository;
    private EncryptionService encryptionService;
    private JWTService jwtService;

    private EmailService emailService;

    public UserService(UsersRepository usersRepository, VerificationTokenRepository verificationTokenRepository, EncryptionService encryptionService, JWTService jwtService, EmailService emailService) {
        this.usersRepository = usersRepository;
        this.verificationTokenRepository=verificationTokenRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public Users registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException, EmailFailureException {
        if(usersRepository.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent() || usersRepository.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()){
            throw new UserAlreadyExistsException();
        }
        Users user = new Users();
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setUsername(registrationBody.getUsername());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        VerificationToken verificationToken = createVerificationToken(user);
        emailService.sendVerificationEmail(verificationToken);
       // verificationTokenRepository.save(verificationToken);
        return usersRepository.save(user);
    }

    private VerificationToken createVerificationToken(Users user){
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);
        return verificationToken;
    }

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {
        Optional<Users> optionalUsers = usersRepository.findByUsernameIgnoreCase(loginBody.getUsername());
        if(optionalUsers.isPresent()){
            Users user = optionalUsers.get();
            if(encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())){
                if(user.getEmailVerified()) {
                    return jwtService.generateJWT(user);
                }
                else {
                    List<VerificationToken> verificationTokenList = user.getVerificationTokens();
                    boolean resend = verificationTokenList.size() == 0
                            || verificationTokenList.get(0).getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis()-(60 * 60 * 1000)));
                    if(resend){
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenRepository.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                    }
                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        return  null;
    }

    @Transactional
    public boolean verifyUser(String token){
        Optional<VerificationToken> optionalVerificationToken = verificationTokenRepository.findByToken(token);
        if(optionalVerificationToken.isPresent()){
            VerificationToken verificationToken = optionalVerificationToken.get();
            Users user = verificationToken.getUser();
            if(!user.getEmailVerified()){
                user.setEmailVerified(true);
                usersRepository.save(user);
                verificationTokenRepository.deleteByUser(user);
                return true;
            }
        }
        return false;
    }

    public void forgotPassword(String email) throws EmailNotFoundException, EmailFailureException {
        Optional<Users> optionalUsers = usersRepository.findByEmailIgnoreCase(email);
        if(optionalUsers.isPresent()){
            Users user = optionalUsers.get();
            String token = jwtService.generatePasswordResetJWT(user);
            emailService.sendPasswordResetEmail(user, token);
        } else{
            throw new EmailNotFoundException();
        }
    }

    public void resetPassword(PasswordResetBody body){
        String email = jwtService.getResetPasswordEmail(body.getToken());
        Optional<Users> opUser = usersRepository.findByEmailIgnoreCase(email);
        if(opUser.isPresent()){
            Users user = opUser.get();
            user.setPassword(encryptionService.encryptPassword(body.getPassword()));
            usersRepository.save(user);
        }
    }

    public boolean userHasPermissionToUser(Users user, Long id){
        return user.getId() == id;
    }
}
