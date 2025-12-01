package com.hyperativa.be.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseEntityDTO {

    private String username;

    private String name;

    private String surname;

    private String email;
}
