package com.test.backend.dto.Request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshRequest {
    private String refreshToken;
}
