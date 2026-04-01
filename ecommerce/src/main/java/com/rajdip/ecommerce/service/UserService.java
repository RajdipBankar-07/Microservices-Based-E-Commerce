package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // READ ALL
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // READ BY ID
    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // UPDATE
    public User updateUser(Long id, User updatedUser){
        User user = getUserById(id);
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setAge(updatedUser.getAge());
        return userRepository.save(user);
    }

    // DELETE
    public String deleteUser(Long id){

        if(userRepository.count() == 0){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User table is empty");
        }

        if(!userRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        userRepository.deleteById(id);
        return "User Deleted Successfully";
    }
}