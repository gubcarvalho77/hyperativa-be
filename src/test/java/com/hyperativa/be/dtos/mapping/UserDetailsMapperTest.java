package com.hyperativa.be.dtos.mapping;

import com.hyperativa.be.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsMapperTest {

    private final UserDetailsMapper mapper = Mappers.getMapper(UserDetailsMapper.class);

    @Test
    void test_to_userDetails() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setName("any name");
        user.setEmail("any@email.com");
        user.setPassword("any-pwd");
        user.setSurname("any surname");
        user.setRole("any role");
        user.setUsername("any username");

        var dto = mapper.toUserDetails(user);

        assertAll(
                () -> assertEquals(user.getUsername(), dto.getUsername()),
                () -> assertEquals(user.getPassword(), dto.getPassword()),
                () -> assertEquals(List.of(user.getRole()), dto.getRoles()),
                () -> assertTrue(dto.isEnabled()),
                () -> assertTrue(dto.isAccountNonExpired()),
                () -> assertTrue(dto.isCredentialsNonExpired()),
                () -> assertTrue(dto.isAccountNonLocked())
        );
    }

    @Test
    void test_when_user_is_null() {
        assertNull(mapper.toUserDetails(null));
    }
}