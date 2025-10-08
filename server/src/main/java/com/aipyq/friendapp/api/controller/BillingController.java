package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.CreateOrderRequest;
import com.aipyq.friendapp.api.dto.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/billing")
public class BillingController {
    private final com.aipyq.friendapp.service.QuotaService quotaService;
    private final java.util.Map<String, String> orderToClient = new java.util.concurrent.ConcurrentHashMap<>();

    public BillingController(com.aipyq.friendapp.service.QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest req, @RequestHeader(value = "X-Client-Id", required = false) String client) {
        if (req.getChannel() == null || !"wechat".equalsIgnoreCase(req.getChannel())) {
            return ResponseEntity.badRequest().build();
        }
        Order o = new Order();
        o.setId(UUID.randomUUID().toString());
        o.setChannel(req.getChannel());
        o.setPlanId(req.getPlanId());
        o.setAmount(9.90);
        o.setStatus("pending");
        o.setCreatedAt(OffsetDateTime.now().toString());
        if (client != null && !client.isBlank()) {
            orderToClient.put(o.getId(), client);
        }
        return ResponseEntity.ok(o);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> query(@PathVariable String id) {
        Order o = new Order();
        o.setId(id);
        o.setChannel("wechat");
        o.setPlanId("basic");
        o.setAmount(9.90);
        o.setStatus("paid");
        o.setCreatedAt(OffsetDateTime.now().minusMinutes(2).toString());
        o.setPaidAt(OffsetDateTime.now().toString());
        return ResponseEntity.ok(o);
    }

    @PostMapping("/callback/{channel}")
    public ResponseEntity<Void> callback(@PathVariable String channel, @RequestParam("orderId") String orderId) {
        if (!"wechat".equalsIgnoreCase(channel)) return ResponseEntity.badRequest().build();
        String client = orderToClient.get(orderId);
        if (client != null) {
            quotaService.grantMonthly(client);
        }
        return ResponseEntity.ok().build();
    }
}
