package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.model.Payment;
import com.ridelink.fare_payment_service.model.Receipt;
import com.ridelink.fare_payment_service.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PaymentService paymentService;

    public ReceiptService(
            ReceiptRepository receiptRepository,
            PaymentService paymentService) {

        this.receiptRepository = receiptRepository;
        this.paymentService = paymentService;
    }

    // Generate receipt for a payment
    public Receipt generateReceipt(String paymentId) {

        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment ID is required"
            );
        }

        // Check whether a receipt already exists
        return receiptRepository.findByPaymentId(paymentId)
                .orElseGet(() -> {

                    Payment payment =
                            paymentService.getPaymentById(paymentId);

                    Receipt receipt = new Receipt(
                            payment.getId(),
                            payment.getRideId(),
                            payment.getAmount(),
                            payment.getStatus()
                    );

                    return receiptRepository.save(receipt);
                });
    }

    // Get receipt by receipt ID
    public Receipt getReceiptById(String receiptId) {

        if (receiptId == null || receiptId.isBlank()) {
            throw new IllegalArgumentException(
                    "Receipt ID is required"
            );
        }

        return receiptRepository.findById(receiptId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Receipt not found: " + receiptId
                        )
                );
    }
}