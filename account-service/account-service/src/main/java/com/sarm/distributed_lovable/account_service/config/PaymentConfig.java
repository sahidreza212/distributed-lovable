package com.sarm.distributed_lovable.account_service.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Value("${stripe.secretKey}")
    private  String stripeSecretkey;


    @PostConstruct
    public void init(){
        Stripe.apiKey = stripeSecretkey;
    }
}
