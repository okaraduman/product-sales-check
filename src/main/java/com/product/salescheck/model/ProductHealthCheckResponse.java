package com.product.salescheck.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductHealthCheckResponse {
    private List<Product> productList;
    private String title;
}
