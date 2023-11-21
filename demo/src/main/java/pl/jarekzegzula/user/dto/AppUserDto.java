package pl.jarekzegzula.user.dto;

import jakarta.validation.constraints.NotEmpty;

public record AppUserDto(
        Integer id,
        @NotEmpty(message = "username is required")
        String username,
        Boolean enabled,
        @NotEmpty(message = "roles are required")
        String roles
) {
}
