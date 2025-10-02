package com.example.demo.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Role {
    private Long id;

    private String title;
    
    private Set<Permission> permissions;
}
