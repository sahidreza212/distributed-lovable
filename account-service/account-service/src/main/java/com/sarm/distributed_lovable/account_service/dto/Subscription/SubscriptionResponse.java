package com.sarm.distributed_lovable.account_service.dto.Subscription;

import com.sarm.distributed_lovable.common_lib.dto.PlanDto;

import java.time.Instant;

public record SubscriptionResponse(
                                   PlanDto plan,
                                   String status,
                                   Instant currentPeriodEnd,
                                   Long tokenUsedThisCycle) {
}
