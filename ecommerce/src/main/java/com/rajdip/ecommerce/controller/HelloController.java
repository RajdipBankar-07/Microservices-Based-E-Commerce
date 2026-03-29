package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.model.User;
import org.springframework.web.bind.annotation.*;
import com.rajdip.ecommerce.service.UserService;

@RestController
@RequestMapping("/api")
public class HelloController {


     private final UserService userService;
     public HelloController(UserService userService){
       this.userService=userService;
    }

    @GetMapping("/hello")
    public String hello(){
    return "New spring boot project start";
}

  @GetMapping("/greet")
    public String greet(@RequestParam String name){
        return "Hello "+name;
  }

  @GetMapping("/user/{id}")
  public String getUser(@PathVariable int id){
        return userService.getUserById(id);
  }

  @PostMapping("/user")
    public String createuser(@RequestBody User user){
        return userService.createUser(user);
  }

}
