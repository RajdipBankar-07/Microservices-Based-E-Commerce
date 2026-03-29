package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
public String createUser(User user){
    return "User created:- "+user.getName()+ " user Id:- "+user.getId();

}

public String getUserById(int id){
    return "user Id from service "+id;
}
}
