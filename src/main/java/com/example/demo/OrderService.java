package com.example.demo;


import com.example.demo.exception.OrderNotFoundException;
import com.example.demo.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.example.demo.outbox.OutboxEventEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OutboxOrderRepository outboxOrderRepository;
    private final EntityManager entityManager;
    private final OrderMapper orderMapper;
    private final OrderService self;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OutboxOrderRepository outboxOrderRepository,
                        EntityManager entityManager,
                        OrderMapper orderMapper,
                        @Lazy OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.outboxOrderRepository = outboxOrderRepository;
        this.entityManager = entityManager;
        this.self = orderService;
        this.orderMapper = orderMapper;
    }

    public OrderDto saveOrder(List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            throw new IllegalArgumentException("Product ids can not be empty");
        }

        return self.saveOrderWithItems(productIds);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderDto saveOrderWithItems(List<Long> productIds) {
        var orderItems = productIds.stream()
                .map(id -> OrderItemEntity.builder()
                        .productId(id)
                        .build())
                .collect(Collectors.toList());

        var order = OrderEntity.builder()
                .build()
                .withOrderItemsEntity(orderItems);
        var savedOrder = orderRepository.save(order);

        var event = OutboxEventEntity.builder()
                .aggregateId(savedOrder.getId())
                .aggregateType("Order")
                .eventType("OrderCreated")
                .payload(ConvertUtil.convertOrderToJson(savedOrder))
                .build();

        outboxOrderRepository.save(event);

        return orderMapper.toDTO(savedOrder);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Long> returnOrder(Long orderId,
                                  Long returnedProductId) {
        if (returnedProductId == null) {
            throw new IllegalArgumentException("Product id can not be null");
        }
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.isIssued()) {
            throw new IllegalArgumentException("Order was already issued");
        }
        for (var orderItem : order.getOrderItems()) {
            if (orderItem.getProductId().equals(returnedProductId)) {
                if (orderItem.isReturned()) {
                    throw new IllegalArgumentException("Product already returned");
                }
                orderItem.setReturned(true);
                publishOrderReturn(orderId, returnedProductId);
                return orderItemRepository.getNotReturnedProductIds(orderId);
            }
        }
        throw new IllegalArgumentException("Product not found in order");
    }

    @Transactional
    public void issueOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        if (order.isIssued()) {
            throw new IllegalArgumentException("Order was already issued");
        }
        order.setIssued(true);
        entityManager.persist(order);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().parallelStream()
                .map(order -> {
                    var productIds = order.getOrderItems().parallelStream()
                            .filter(orderItem -> !orderItem.isReturned())
                            .map(OrderItemEntity::getProductId)
                            .collect(Collectors.toList());
                    return new OrderDto(order.getId(), productIds);
                })
                .collect(Collectors.toList());
    }

    private void publishOrderCreation(OrderEntity order) {
        //Внутри метода происходит отправка данных о создания заказа в платежную систему
    }

    private void publishOrderReturn(long orderId,
                                    long productId) {
        //Внутри метода происходит запрос на возврат средств по товару в заказе
    }
}
