package com.sarm.distributed_lovable.account_service.dto.Auth;

public record AuthResponse(
        String token ,
        UserProfileResponse user
) {
}
