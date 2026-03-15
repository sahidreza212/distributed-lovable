package com.sarm.distributed_lovable.account_service.serviceImpl;


import com.sarm.distributed_lovable.account_service.dto.Subscription.SubscriptionResponse;
import com.sarm.distributed_lovable.account_service.entity.Plan;
import com.sarm.distributed_lovable.account_service.entity.Subscription;
import com.sarm.distributed_lovable.account_service.entity.User;
import com.sarm.distributed_lovable.account_service.mappers.SubscriptionMapper;
import com.sarm.distributed_lovable.account_service.repository.PlanRepository;
import com.sarm.distributed_lovable.account_service.repository.SubscriptionRepository;
import com.sarm.distributed_lovable.account_service.repository.UserRepository;
import com.sarm.distributed_lovable.account_service.service.SubscriptionService;
import com.sarm.distributed_lovable.common_lib.dto.PlanDto;
import com.sarm.distributed_lovable.common_lib.enums.SubscriptionStatus;
import com.sarm.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.sarm.distributed_lovable.common_lib.security.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private  final AuthUtil authUtil;
    private final SubscriptionRepository subscriptionRepository;
    private  final UserRepository userRepository;
    private  final PlanRepository planRepository;
    private final SubscriptionMapper subscriptionMapper;
   // private final ProjectMemberRepository projectMemberRepository;


    private final Integer FREE_TIER_PROJECTS_ALLOWED = 100;



    @Override
    public SubscriptionResponse getCurrentSubscription() {

        Long userId = authUtil.getCurrentUserId();

       var currentSubscription = subscriptionRepository.findByUserIdAndStatusIn(userId,Set.of(SubscriptionStatus.ACTIVE,SubscriptionStatus.PAST_DUE,SubscriptionStatus
                .TRIALING)).orElse(new Subscription()
       );


        return subscriptionMapper.toSubscriptionResponse(currentSubscription);
    }

    @Override
    public void activateSubscription(Long userId, Long planId, String customerId, String subscriptionId) {

    boolean exists = subscriptionRepository.existsByStripeSubscriptionId(subscriptionId);
    if(exists)return;
    User user = getUser(userId);
    Plan plan = getPlan(planId);

    Subscription subscription = Subscription.builder()
            .user(user)
            .plan(plan)
            .stripeSubscriptionId(subscriptionId)
            .status(SubscriptionStatus.INCOMPLETE)
            .build();
    subscriptionRepository.save(subscription);

    }

    @Override
    @Transactional
    public void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {

        Subscription subscription = getSubscription(gatewaySubscriptionId);
        boolean hasSubscriptionUpdate = false;

        if(status != null && status != subscription.getStatus()){
            subscription.setStatus(status);
            hasSubscriptionUpdate = true;
        }

        if(periodStart != null && !periodStart.equals(subscription.getCurrentPeriodStart())){
            subscription.setCurrentPeriodStart(periodStart);
            hasSubscriptionUpdate = true;

        }

        if(periodEnd != null && !periodEnd.equals(subscription.getCurrentPeriodEnd())){
            subscription.setCurrentPeriodEnd(periodEnd);
            hasSubscriptionUpdate = true;
        }
        if(cancelAtPeriodEnd != null && cancelAtPeriodEnd != subscription.getCancelAtPeriodEnd()) {
            subscription.setCurrentPeriodEnd(periodEnd);
            hasSubscriptionUpdate = true;
        }

        if(planId != null && !planId.equals(subscription.getPlan().getId())){
            Plan newPlan = getPlan(planId);
            subscription.setPlan(newPlan);
            hasSubscriptionUpdate = true;
        }

        if(hasSubscriptionUpdate){
            log.debug("Subscription has been updated :{}",gatewaySubscriptionId);
            subscriptionRepository.save(subscription);
        }
    }

    @Override
    public void cancelSubscription(String gatewaySubscriptionId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);

    }

    @Override
    public void renewSubscriptionPeriod(String gatewaySubscriptionId, Instant periodStart, Instant periodEnd) {

        Subscription subscription = getSubscription(gatewaySubscriptionId);

        Instant newStart = periodStart != null ? periodStart:subscription.getCurrentPeriodEnd();
        subscription.setCurrentPeriodStart(newStart);
        subscription.setCurrentPeriodEnd(periodEnd);

        if(subscription.getStatus() == SubscriptionStatus.PAST_DUE || subscription.getStatus() == SubscriptionStatus.INCOMPLETE){
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        subscriptionRepository.save(subscription);

    }

    @Override
    public void markSubscriptionPostDue(String gatewaySubscriptionId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);
        if(subscription.getStatus()==SubscriptionStatus.PAST_DUE){
            log.debug("Subscription is already past due , gatewaySubscriptionId: {}",gatewaySubscriptionId);
            return;
        }
        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);
    }

    @Override
    public PlanDto getCurrentSubscribedPlanByUser() {
        SubscriptionResponse subscriptionResponse = getCurrentSubscription();
        return subscriptionResponse.plan();

    }


    // Internal method ----->

    private User  getUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User",userId.toString()));
    }
    private Plan  getPlan(Long planId){
        return planRepository.findById(planId)
                .orElseThrow(()-> new ResourceNotFoundException("Plan",planId.toString()));
    }

    private Subscription getSubscription(String gatewaySubscriptionId){
        return subscriptionRepository.findByStripeSubscriptionId(gatewaySubscriptionId)
                .orElseThrow(()->new  ResourceNotFoundException("Subscription",gatewaySubscriptionId));
    }

}
