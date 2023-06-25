package mettech_intl.service;

import mettech_intl.api.models.RegistrationBody;
import mettech_intl.model.dao.UsersRepository;
import mettech_intl.exception.UserAlreadyExistsException;
import mettech_intl.model.Users;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UsersRepository usersRepository;

    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
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
        //TODO: Criptarea parolei!!
        user.setPassword(registrationBody.getPassword());
        user = usersRepository.save(user);
        return user;
    }
}
