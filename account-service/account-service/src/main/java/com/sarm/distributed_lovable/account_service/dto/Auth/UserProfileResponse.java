package com.sarm.distributed_lovable.account_service.dto.Auth;

public record UserProfileResponse(
                                  Long id,
                                  String username,
                                  String name
                                  ) {
}
