package com.sarm.distributed_lovable.account_service.mappers;


import com.sarm.distributed_lovable.account_service.dto.Subscription.SubscriptionResponse;
import com.sarm.distributed_lovable.account_service.entity.Plan;
import com.sarm.distributed_lovable.account_service.entity.Subscription;
import com.sarm.distributed_lovable.common_lib.dto.PlanDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PlanDto toPlanResponse(Plan plan);

}
