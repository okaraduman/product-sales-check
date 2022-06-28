package com.product.salescheck.service.impl;


import com.product.salescheck.model.Product;
import com.product.salescheck.model.ProductHealthCheckResponse;
import com.product.salescheck.service.ProductService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final String FILE_NAME = "GrouponDeals.xlsx";
    private ProductHealthCheckResponse healthCheckResponse;

    public ProductServiceImpl() {
        healthCheckResponse = getProductList();
    }

    @Override
    public ProductHealthCheckResponse checkAll() {
        healthCheckResponse.getProductList().parallelStream().forEach(product -> {
            checkProduct(product);
        });
        createOutputFile();
        return healthCheckResponse;
    }

    public void checkProduct(Product product) {
        try {
            Document document = getWebSite(product.getUrl());
            getPrice(document);
            product.setOnSale(true);
        } catch (Exception e) {
            product.setOnSale(false);
        }
    }

    public ProductHealthCheckResponse getProductList() {
        ProductHealthCheckResponse response = new ProductHealthCheckResponse();
        List<Product> productList = new ArrayList<>();
        try {
            FileInputStream excelFile = new FileInputStream(new File(getClass().getClassLoader().getResource(FILE_NAME).toURI()));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
            Row row = iterator.next();
            Iterator<Cell> cell = row.iterator();
            response.setTitle(getCellValue(cell.next()));

            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                Product product = new Product();
                product.setUrl(getCellValue(cellIterator.next()));

                productList.add(product);
            }
            response.setProductList(productList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return response;
    }


    private String getCellValue(Cell cell) {
        if (cell.getCellTypeEnum() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return null;
    }



    private Document getWebSite(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla").get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPrice(Document document) {
        //String price = document.getElementsByClass("breakout-option-price c-txt-price").get(0).html();
        String price = document.getElementsByClass("breakout-option-price").get(0).html();
        if (price.contains("nbsp")) {
            price = price.replace("&nbsp;", " ");
        }
        return price;
    }


    private void createOutputFile() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Output");
        int rowCount = 0;
        Row header = sheet.createRow(rowCount++);
        header.createCell(0).setCellValue(healthCheckResponse.getTitle());

        for (Product product : healthCheckResponse.getProductList()) {
            int columnCount = 0;
            Row row = sheet.createRow(rowCount++);
            row.createCell(columnCount++).setCellValue(product.getUrl());
            row.createCell(columnCount++).setCellValue(product.getOnSale());
        }

        try {
            FileOutputStream out = new FileOutputStream(new File("Output.xlsx"));
            workbook.write(out);
            out.close();
            System.out.println("Excel with formula cells written successfully");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
