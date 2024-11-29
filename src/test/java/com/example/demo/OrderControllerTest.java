package com.example.demo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class OrderControllerTest {

    @Autowired
    OrderController orderController;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Test
    void createOrder() {
        var product = List.of(1L, 2L, 3L, 4L);
        orderController.createOrder(product);

        var orders = orderRepository.findAll();
        Assertions.assertThat(orders).hasSize(1);

        var orderItems = orderItemRepository.findAll();
        Assertions.assertThat(orderItems).hasSize(product.size());
        for (OrderItemEntity orderItem : orderItems) {
            Assertions.assertThat(orderItem.getOrder()).isEqualTo(orders.get(0));
        }
    }
}