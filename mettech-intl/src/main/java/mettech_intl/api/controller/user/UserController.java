package mettech_intl.api.controller.user;

import mettech_intl.model.Address;
import mettech_intl.model.Users;
import mettech_intl.model.dao.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private AddressRepository addressRepository;

    public UserController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(@AuthenticationPrincipal Users user, @PathVariable Long userId){
        if(!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressRepository.findByUser_Id(userId));
    }

    @PutMapping("/{userId}/address")
    public ResponseEntity<Address> putAddress(@AuthenticationPrincipal Users user, @PathVariable Long userId, @RequestBody Address address){
        if(!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        Users refUser = new Users();
        refUser.setId(userId);
        address.setUser(refUser);
        return ResponseEntity.ok(addressRepository.save(address));
    }


    @PatchMapping("/{userId}/address/{addressId}")
    public ResponseEntity<Address> patchAddress(@AuthenticationPrincipal Users user, @PathVariable Long userId, @PathVariable Long addressId,@RequestBody Address address){
        if(!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(address.getId() == addressId){
            Optional<Address> opOriginalAddress = addressRepository.findById(addressId);
            if(opOriginalAddress.isPresent()){
                Users originalUser = opOriginalAddress.get().getUser();
                if(originalUser.getId()== userId){
                    address.setUser(originalUser);
                    return  ResponseEntity.ok(addressRepository.save(address));
                }

            }
        }
        return ResponseEntity.badRequest().build();
    }

    private boolean userHasPermission(Users user, Long id){
        return user.getId() == id;
    }

}
