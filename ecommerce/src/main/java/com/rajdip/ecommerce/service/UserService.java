package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    // Register User
    public User register(User user) {
        return repo.save(user);
    }

    // Login Logic
    public String login(String email, String password) {
        Optional<User> user = repo.findByEmail(email);

        if (user.isPresent()) {
            if (user.get().getPassword().equals(password)) {
                return "Login Successful";
            } else {
                return "Wrong Password";
            }
        } else {
            return "User Not Found";
        }
    }
}
