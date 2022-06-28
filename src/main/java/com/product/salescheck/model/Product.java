package com.product.salescheck.model;

import lombok.Data;

@Data
public class Product {
    private String url;
    private Object image;
    private Boolean onSale;
}
