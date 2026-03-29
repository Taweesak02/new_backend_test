package com.test.backend.dto.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min=8, message = "Must be at least 8 characters")
    private String password;
}
