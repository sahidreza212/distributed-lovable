package com.sarm.distributed_lovable.account_service.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @Email@NotBlank(message = "User name must not be blank")
          String username,
       @Size(min = 1,max = 50) String name,
        @Size(min = 4) String password
) {
}
