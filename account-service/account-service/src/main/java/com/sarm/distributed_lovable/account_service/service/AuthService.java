package com.sarm.distributed_lovable.account_service.service;


import com.sarm.distributed_lovable.account_service.dto.Auth.AuthResponse;
import com.sarm.distributed_lovable.account_service.dto.Auth.LoginRequest;
import com.sarm.distributed_lovable.account_service.dto.Auth.SignupRequest;

public interface AuthService {
     AuthResponse Signup(SignupRequest request);


     AuthResponse login(LoginRequest request);
}
