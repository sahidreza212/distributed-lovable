package com.sarm.distributed_lovable.account_service.controller;


import com.sarm.distributed_lovable.account_service.dto.Auth.AuthResponse;
import com.sarm.distributed_lovable.account_service.dto.Auth.LoginRequest;
import com.sarm.distributed_lovable.account_service.dto.Auth.SignupRequest;
import com.sarm.distributed_lovable.account_service.dto.Auth.UserProfileResponse;
import com.sarm.distributed_lovable.account_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")

public class AuthController {

    private final AuthService authService;
  //  private  final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> Signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.Signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

//    @GetMapping("/me")
//    public ResponseEntity<UserProfileResponse>getProfile(){
//        Long userId = 1L;
//        return ResponseEntity.ok(userService.getProfile(userId));
//    }




}