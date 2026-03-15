package com.sarm.distributed_lovable.account_service.repository;


import com.sarm.distributed_lovable.account_service.entity.User;
import com.sarm.distributed_lovable.common_lib.dto.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String email);


    Optional<User> findByUsernameIgnoreCase(String email);
}