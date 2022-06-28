package com.product.salescheck.web;

import com.product.salescheck.model.ProductHealthCheckResponse;
import com.product.salescheck.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping("check")
    public ProductHealthCheckResponse check(){
        System.out.println(Instant.now());
        ProductHealthCheckResponse response = productService.checkAll();
        System.out.println(Instant.now());
        return response;
    }
}
