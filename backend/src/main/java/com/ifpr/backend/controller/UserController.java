package com.ifpr.backend.controller;

import static com.ifpr.backend.dto.AuthDtos.MessageResponse;
import static com.ifpr.backend.dto.UserDtos.*;

import com.ifpr.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @GetMapping("/me")
    public UserResponse me() { 
        return service.me(); 
    }

    @PutMapping("/me")
    public UserResponse update(@Valid @RequestBody UpdateUserRequest request) { 
        return service.update(request); 
    }

    @PatchMapping("/me/password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return service.changePassword(request);
    }
}
