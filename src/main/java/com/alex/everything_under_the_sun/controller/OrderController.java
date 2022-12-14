package com.alex.everything_under_the_sun.controller;

import com.alex.everything_under_the_sun.dto.OrderProductDto;
import com.alex.everything_under_the_sun.model.Order;
import com.alex.everything_under_the_sun.model.OrderProduct;
import com.alex.everything_under_the_sun.service.OrderProductService;
import com.alex.everything_under_the_sun.service.OrderService;
import com.alex.everything_under_the_sun.service.ProductService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    ProductService productService;
    OrderService orderService;
    OrderProductService orderProductService;

    public OrderController(ProductService productService, OrderService orderService, OrderProductService orderProductService)
    {
        this.productService = productService;
        this.orderService = orderService;
        this.orderProductService = orderProductService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public @NotNull Iterable<Order> list()
    {
        return this.orderService.getAllOrders();
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody OrderForm form)
    {
        List<OrderProductDto> formDto = form.getProductOrders();
        validateProductsExistence(formDto);

        Order order = new Order();
        order.setStatus("PAID");
        order = this.orderService.create(order);

        List<OrderProduct> orderProducts = new ArrayList<>();
        for (OrderProductDto dto : formDto)
        {
            orderProducts.add(orderProductService.create(new OrderProduct(order,
                    productService.getProduct(dto
                            .getProduct()
                            .getId()),
                    dto.getQuantity())));
        }

        order.setOrderProducts(orderProducts);

        this.orderService.update(order);

        String uri = ServletUriComponentsBuilder
                .fromCurrentServletMapping()
                .path("/orders/{id}")
                .buildAndExpand(order.getId())
                .toString();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", uri);

        return new ResponseEntity<>(order, headers, HttpStatus.CREATED);
    }

    private void validateProductsExistence(@NotNull List<OrderProductDto> orderProducts)
    {
        List<OrderProductDto> list = orderProducts
                .stream()
                .filter(op -> Objects.isNull(productService.getProduct(op
                        .getProduct()
                        .getId())))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(list))
        {
            throw new ResourceNotFoundException("Product not found");
        }
    }

    public static class OrderForm {

        private List<OrderProductDto> productOrders;

        public List<OrderProductDto> getProductOrders()
        {
            return productOrders;
        }

        public void setProductOrders(List<OrderProductDto> productOrders)
        {
            this.productOrders = productOrders;
        }
    }
}
