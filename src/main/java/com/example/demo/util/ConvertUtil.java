package com.example.demo.util;

import com.example.demo.OrderEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvertUtil {
    public static String convertOrderToJson(OrderEntity order) {
        try {
            return new ObjectMapper().writeValueAsString(order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing order", e);
        }
    }
}
