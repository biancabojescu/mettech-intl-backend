package mettech_intl.service;

import mettech_intl.api.models.LoginBody;
import mettech_intl.api.models.RegistrationBody;
import mettech_intl.model.dao.UsersRepository;
import mettech_intl.exception.UserAlreadyExistsException;
import mettech_intl.model.Users;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private UsersRepository usersRepository;
    private EncryptionService encryptionService;
    private JWTService jwtService;

    public UserService(UsersRepository usersRepository, EncryptionService encryptionService, JWTService jwtService) {
        this.usersRepository = usersRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
    }

    public Users registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if(usersRepository.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent() || usersRepository.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()){
            throw new UserAlreadyExistsException();
        }
        Users user = new Users();
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setUsername(registrationBody.getUsername());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        user = usersRepository.save(user);
        return user;
    }

    public String loginUser(LoginBody loginBody){
        Optional<Users> optionalUsers = usersRepository.findByUsernameIgnoreCase(loginBody.getUsername());
        if(optionalUsers.isPresent()){
            Users user = optionalUsers.get();
            if(encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())){
                return jwtService.generateJWT(user);
            }
        }
        return  null;
    }
}
