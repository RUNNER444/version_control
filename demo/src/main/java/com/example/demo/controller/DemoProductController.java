package com.example.demo.controller;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.DemoProduct;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class DemoProductController {
    private List<DemoProduct> products = new ArrayList<>(Arrays.asList(
        new DemoProduct(1l, "smth", 100),
        new DemoProduct(2l, "smth2", 200)
    ));

    @GetMapping("/products")
    public List<DemoProduct> getProducts() {
        return products;
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<DemoProduct> getProduct(@PathVariable Long id) {
        for (DemoProduct product : products) {
            if (product.getId().equals(id)) {
                return ResponseEntity.ok(product);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/products")
    public ResponseEntity<DemoProduct> addProduct(@RequestBody @Valid DemoProduct product) {
        product.setId((long)products.size() + 1);
        products.add(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    
    
}
