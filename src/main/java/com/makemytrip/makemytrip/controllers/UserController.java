package com.makemytrip.makemytrip.controllers;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.services.UserServices;

@RestController
@RequestMapping("/user")

public class UserController {

    @Autowired
    private UserServices userServices;

    @PostMapping("/login")
    public Users login(@RequestParam String email, @RequestParam String password) {
        return userServices.login(email, password);
    }

    @PostMapping("/signup")
    public ResponseEntity<Users> signup(@RequestBody Users user) {
        return ResponseEntity.ok(userServices.signup(user));
    }
}
