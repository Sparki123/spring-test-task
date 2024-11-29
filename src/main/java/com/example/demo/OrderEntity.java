package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Setter
@Getter
@Entity
public class OrderEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(nullable = false, unique = true)
    private Long id;

    @Builder.Default
    private boolean issued = false;

    @OneToMany(cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "order",
            fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    public OrderEntity withOrderItemsEntity(List<OrderItemEntity> orderItems) {
        for (OrderItemEntity orderItemEntity: orderItems) {
            this.orderItems.add(orderItemEntity);
            orderItemEntity.setOrder(this);
        }

        return this;
    }
}
