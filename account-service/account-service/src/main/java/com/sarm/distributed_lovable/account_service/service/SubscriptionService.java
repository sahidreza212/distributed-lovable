package com.sarm.distributed_lovable.account_service.service;


import com.sarm.distributed_lovable.account_service.dto.Subscription.SubscriptionResponse;
import com.sarm.distributed_lovable.common_lib.dto.PlanDto;
import com.sarm.distributed_lovable.common_lib.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {

     SubscriptionResponse getCurrentSubscription();


    void activateSubscription(Long userId, Long planId, String customerId, String subscriptionId);

    void  updateSubscription
            (
                    String gatewaySubscriptionId, SubscriptionStatus status, Instant periodStart,
                    Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId
            );

    void cancelSubscription(String gatewaySubscriptionId);

    void renewSubscriptionPeriod(String subId, Instant periodStart, Instant periodEnd);

    void markSubscriptionPostDue(String gatewaySubscriptionId);


    PlanDto getCurrentSubscribedPlanByUser();

}
