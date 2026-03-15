package com.sarm.distributed_lovable.account_service.mappers;


import com.sarm.distributed_lovable.account_service.dto.Auth.SignupRequest;
import com.sarm.distributed_lovable.account_service.dto.Auth.UserProfileResponse;
import com.sarm.distributed_lovable.account_service.entity.User;
import com.sarm.distributed_lovable.common_lib.dto.UserDto;
import com.sarm.distributed_lovable.common_lib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest signupRequest);

    @Mapping(source = "userId", target = "id")
    UserProfileResponse toUserProfileResponse(JwtUserPrincipal user);

    UserDto toUserDto(User user);
}
