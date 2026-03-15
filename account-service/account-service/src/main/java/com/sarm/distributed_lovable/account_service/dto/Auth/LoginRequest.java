package com.sarm.distributed_lovable.account_service.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
      @NotBlank@Email String username,
       @Size(min = 4,max = 50) String password
) {
}
