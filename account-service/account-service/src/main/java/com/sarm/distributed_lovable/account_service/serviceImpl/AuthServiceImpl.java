package com.sarm.distributed_lovable.account_service.serviceImpl;


import com.sarm.distributed_lovable.account_service.dto.Auth.AuthResponse;
import com.sarm.distributed_lovable.account_service.dto.Auth.LoginRequest;
import com.sarm.distributed_lovable.account_service.dto.Auth.SignupRequest;
import com.sarm.distributed_lovable.account_service.entity.User;
import com.sarm.distributed_lovable.account_service.mappers.UserMapper;
import com.sarm.distributed_lovable.account_service.repository.UserRepository;
import com.sarm.distributed_lovable.account_service.service.AuthService;
import com.sarm.distributed_lovable.common_lib.error.BadRequestException;
import com.sarm.distributed_lovable.common_lib.security.AuthUtil;
import com.sarm.distributed_lovable.common_lib.security.JwtUserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    AuthUtil authUtil;
    AuthenticationManager authenticationManager;


    @Override
    public AuthResponse Signup(SignupRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(
                user -> {throw new BadRequestException("User already exists with username "+request.username());
                });

        User user  = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal(user.getId(),user.getName(),
                user.getUsername(),null,new ArrayList<>());


        String token = authUtil.generateAccessToken(jwtUserPrincipal);
        return new AuthResponse(token,userMapper.toUserProfileResponse(jwtUserPrincipal));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.username(),request.password()));

        JwtUserPrincipal user  = (JwtUserPrincipal) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(user);

        return new AuthResponse(token,userMapper.toUserProfileResponse(user));

    }
}
