package io.github.kuonjiarisu.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.github.kuonjiarisu.backend.auth.model.UserAccount;

@Mapper
public interface UserMapper {

    UserAccount findByUsername(@Param("username") String username);

    UserAccount findById(@Param("id") String id);

    int countAll();

    void insert(UserAccount user);

    void updateLastLoginAt(@Param("id") String id, @Param("lastLoginAt") java.time.LocalDateTime lastLoginAt);
}
