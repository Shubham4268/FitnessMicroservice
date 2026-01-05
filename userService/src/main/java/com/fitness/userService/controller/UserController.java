package com.fitness.userService.controller;

import com.fitness.userService.dto.RegisterRequest;
import com.fitness.userService.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fitness.userService.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    private ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PostMapping("/register")
    private ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @GetMapping("/{userId}/validate")
    private ResponseEntity<Boolean> validateUser(@PathVariable String userId){
        return ResponseEntity.ok(userService.existByUserUd(userId));
    }
}