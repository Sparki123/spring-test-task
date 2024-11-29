package com.example.demo;

import com.example.demo.outbox.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxOrderRepository extends JpaRepository<OutboxEventEntity, Long> {
}
