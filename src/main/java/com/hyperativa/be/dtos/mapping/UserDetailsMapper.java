package com.hyperativa.be.dtos.mapping;

import com.hyperativa.be.dtos.UserDetailsDTO;
import com.hyperativa.be.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserDetailsMapper {

    @BeforeMapping
    default void beforeMappingDTO(
            @MappingTarget UserDetailsDTO target,
            User source
    ) {
        target.setFullName(
                "%s %s".formatted(source.getName(), source.getSurname())
        );
        target.setRoles(List.of(source.getRole()));
    }

    @Mapping(target = "enabled", constant = "true")
    UserDetailsDTO toUserDetails(User user);
}
