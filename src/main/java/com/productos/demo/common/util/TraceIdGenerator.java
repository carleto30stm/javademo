package com.productos.demo.common.util;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TraceIdGenerator {

    public String generate() {
        return "req-" + UUID.randomUUID().toString().substring(0, 13);
    }
}
