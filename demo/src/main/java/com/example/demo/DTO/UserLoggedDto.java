package com.example.demo.DTO;

import java.util.Set;

public record UserLoggedDto(String username,
String role,
Set <String> permissions) {

}
