package com.makemytrip.makemytrip.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

public class RootController {

    @GetMapping("/")
    public String home() {
        return "Welcome to MakeMyTrip API";
    }
}
