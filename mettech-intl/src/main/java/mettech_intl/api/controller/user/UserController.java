package mettech_intl.api.controller.user;

import mettech_intl.api.models.DataChange;
import mettech_intl.model.Address;
import mettech_intl.model.Users;
import mettech_intl.model.dao.AddressRepository;
import mettech_intl.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private AddressRepository addressRepository;
    private SimpMessagingTemplate simpMessagingTemplate;
    private UserService userService;

    public UserController(AddressRepository addressRepository, SimpMessagingTemplate simpMessagingTemplate, UserService userService) {
        this.addressRepository = addressRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(@AuthenticationPrincipal Users user, @PathVariable Long userId){
        if(!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressRepository.findByUser_Id(userId));
    }

    /**
     * INSERT
     * @param user
     * @param userId
     * @param address
     * @return
     */
    @PutMapping("/{userId}/address")
    public ResponseEntity<Address> putAddress(@AuthenticationPrincipal Users user, @PathVariable Long userId, @RequestBody Address address){
        if(!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        Users refUser = new Users();
        refUser.setId(userId);
        address.setUser(refUser);
        Address savedAddress = addressRepository.save(address);
        simpMessagingTemplate.convertAndSend("/topic/user/" + userId + "/address", new DataChange<>(DataChange.ChangeType.INSERT, address));
        return ResponseEntity.ok(savedAddress);
    }

    /**
     * UPDATE
     * @param user
     * @param userId
     * @param addressId
     * @param address
     * @return
     */
    @PatchMapping("/{userId}/address/{addressId}")
    public ResponseEntity<Address> patchAddress(@AuthenticationPrincipal Users user, @PathVariable Long userId, @PathVariable Long addressId,@RequestBody Address address){
        if(!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(address.getId() == addressId){
            Optional<Address> opOriginalAddress = addressRepository.findById(addressId);
            if(opOriginalAddress.isPresent()){
                Users originalUser = opOriginalAddress.get().getUser();
                if(originalUser.getId()== userId){
                    address.setUser(originalUser);
                    Address savedAddress = addressRepository.save(address);
                    simpMessagingTemplate.convertAndSend("/topic/user/" + userId + "/address", new DataChange<>(DataChange.ChangeType.UPDATE, address));
                    return ResponseEntity.ok(savedAddress);
                }

            }
        }
        return ResponseEntity.badRequest().build();
    }
}
