package com.sarm.distributed_lovable.account_service.controller;

import com.sarm.distributed_lovable.account_service.mappers.UserMapper;
import com.sarm.distributed_lovable.account_service.repository.UserRepository;
import com.sarm.distributed_lovable.account_service.service.SubscriptionService;
import com.sarm.distributed_lovable.common_lib.dto.PlanDto;
import com.sarm.distributed_lovable.common_lib.dto.UserDto;
import com.sarm.distributed_lovable.common_lib.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
public class InternalAccountController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private  final SubscriptionService subscriptionService;

    @GetMapping("/users/{id}")
    public UserDto getUserById(@PathVariable Long id){
        return userRepository.findById(id).map(userMapper::toUserDto)
                .orElseThrow(()->new ResourceNotFoundException("User",id.toString()));
    }

    @GetMapping("/users/by-email")
    public Optional<UserDto>getUserByEmail(@RequestParam String email){
        return userRepository.findByUsernameIgnoreCase(email)
                .map(user -> userMapper.toUserDto(user));

    }
    @GetMapping("/billing/current-plan")
    public PlanDto getCurrentSubscribedPlan() {
        return subscriptionService.getCurrentSubscribedPlanByUser();
    }
}
