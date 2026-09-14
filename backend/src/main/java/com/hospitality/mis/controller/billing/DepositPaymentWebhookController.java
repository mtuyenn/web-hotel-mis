package com.hospitality.mis.controller.billing;

import com.hospitality.mis.dto.billing.DepositPaymentWebhookDtos;
import com.hospitality.mis.service.billing.DepositPaymentWebhookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Webhook public của provider; quyền truy cập được thay bằng HMAC signature. */
@RestController
@RequestMapping("/api/public/payment-callbacks")
public class DepositPaymentWebhookController {
    private final DepositPaymentWebhookService service;

    public DepositPaymentWebhookController(DepositPaymentWebhookService service) { this.service = service; }

    @PostMapping("/deposit")
    public DepositPaymentWebhookDtos.Response deposit(@Valid @RequestBody DepositPaymentWebhookDtos.Request request,
                                                       @RequestHeader(value = "X-Payment-Signature", required = false) String signature) {
        return service.accept(request, signature);
    }
}
