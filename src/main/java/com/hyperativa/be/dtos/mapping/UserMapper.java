package com.hyperativa.be.dtos.mapping;

import com.hyperativa.be.dtos.UserDTO;
import com.hyperativa.be.model.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface UserMapper {

    @Mapping(target = "enabled", constant = "true")
    UserDTO toDTO(User user);
}
