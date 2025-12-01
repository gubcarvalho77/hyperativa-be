package com.hyperativa.be.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
public class User extends BaseEntity {

    private String username;

    private String name;

    private String surname;

    @Column(unique = true,  nullable = false)
    private String email;

    @JsonIgnore
    @ToString.Exclude
    private String password;

    private String role;
}
