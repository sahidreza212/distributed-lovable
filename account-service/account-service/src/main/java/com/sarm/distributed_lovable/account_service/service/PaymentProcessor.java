package com.sarm.distributed_lovable.account_service.service;


import com.sarm.distributed_lovable.account_service.dto.Subscription.CheckOutRequest;
import com.sarm.distributed_lovable.account_service.dto.Subscription.CheckOutResponse;
import com.sarm.distributed_lovable.account_service.dto.Subscription.PortalResponse;
import com.stripe.model.StripeObject;

import java.util.Map;

public interface PaymentProcessor {


    CheckOutResponse createCheckOutUrl(CheckOutRequest request);

    PortalResponse openCustomerPortal();

    void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata);
}
