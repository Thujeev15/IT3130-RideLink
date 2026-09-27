package com.ridelink.fare_payment_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.model.Receipt;
import com.ridelink.fare_payment_service.service.ReceiptService;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping("/payment/{paymentId}")
    public ResponseEntity<ReceiptResponse> generateReceipt(
            @PathVariable String paymentId) {

        Receipt receipt = receiptService.generateReceipt(paymentId);

        return ResponseEntity.ok(toResponse(receipt));
    }

    @GetMapping("/{receiptId}")
    public ResponseEntity<ReceiptResponse> getReceipt(
            @PathVariable String receiptId) {

        Receipt receipt = receiptService.getReceiptById(receiptId);

        return ResponseEntity.ok(toResponse(receipt));
    }

    private ReceiptResponse toResponse(Receipt receipt) {

        return new ReceiptResponse(
                receipt.getId(),
                receipt.getPaymentId(),
                receipt.getRideId(),
                receipt.getAmount(),
                receipt.getPaymentStatus(),
                receipt.getGeneratedAt()
        );
    }
}