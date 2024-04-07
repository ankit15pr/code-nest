package com.api.codenest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user){

        String password = user.getPassword();
        String confirmPassword = user.getConfirmpassword();
        
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("Passwords don't match");
        }

       
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}
