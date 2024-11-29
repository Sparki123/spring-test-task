package com.example.demo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target="orderId", source = "id")
    @Mapping(target = "productIds", source = "orderItems")
    OrderDto toDTO(OrderEntity order);

    @Mapping(target="id", source = "orderId")
    @Mapping(target = "orderItems", source = "productIds")
    OrderEntity toEntity(OrderDto order);

    default Long orderItemEntityToLong(OrderItemEntity orderItem) {
        return orderItem.getProductId();
    }

    default OrderItemEntity longToOrderItemEntity(Long productId) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setProductId(productId);
        return orderItem;
    }

    default List<Long> orderItemEntitiesToLongs(List<OrderItemEntity> orderItems) {
        if (orderItems == null) {
            return null;
        }
        return orderItems.stream()
                .map(this::orderItemEntityToLong)
                .collect(Collectors.toList());
    }

    default List<OrderItemEntity> longsToOrderItemEntities(List<Long> productIds) {
        if (productIds == null) {
            return null;
        }
        return productIds.stream()
                .map(this::longToOrderItemEntity)
                .collect(Collectors.toList());
    }

}
