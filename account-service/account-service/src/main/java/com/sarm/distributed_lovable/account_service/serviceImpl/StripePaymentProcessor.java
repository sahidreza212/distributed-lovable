package com.sarm.distributed_lovable.account_service.serviceImpl;




import com.sarm.distributed_lovable.account_service.dto.Subscription.CheckOutRequest;
import com.sarm.distributed_lovable.account_service.dto.Subscription.CheckOutResponse;
import com.sarm.distributed_lovable.account_service.dto.Subscription.PortalResponse;
import com.sarm.distributed_lovable.account_service.repository.PlanRepository;
import com.sarm.distributed_lovable.account_service.entity.Plan;
import com.sarm.distributed_lovable.account_service.entity.User;
import com.sarm.distributed_lovable.account_service.repository.UserRepository;
import com.sarm.distributed_lovable.account_service.service.PaymentProcessor;
import com.sarm.distributed_lovable.account_service.service.SubscriptionService;

import com.sarm.distributed_lovable.common_lib.enums.SubscriptionStatus;
import com.sarm.distributed_lovable.common_lib.error.BadRequestException;
import com.sarm.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.sarm.distributed_lovable.common_lib.security.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentProcessor implements PaymentProcessor {

    private  final AuthUtil authUtil;
    private  final PlanRepository planRepository;
    private  final UserRepository userRepository;
    private  final SubscriptionService subscriptionService;

    @Value("${app.frontend.url}")
    private  String frontendUrl;

    @Override
    public CheckOutResponse createCheckOutUrl(CheckOutRequest request) {
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(()->new ResourceNotFoundException("Plan",request.planId().toString()));

        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("userId",userId.toString()));
        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(plan.getStripePriceId()).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build())
                                .build()
                )
                .setSuccessUrl(frontendUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/cancel.html")
                .putMetadata("user_id", userId.toString())
                .putMetadata("plan_id", plan.getId().toString());
        try {
            String stripeCustomerId =  user.getStripeCustomerId();
            if(stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                params.setCustomerEmail(user.getUsername());
            } else {
                params.setCustomer(stripeCustomerId); // stripe customer Id
            }

            Session session = Session.create(params.build()); // making api call to the Stripe Backend
            return new CheckOutResponse(session.getUrl());

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public PortalResponse openCustomerPortal() {
        Long userId = authUtil.getCurrentUserId();
        User user = getUser(userId);

        String stripeCustomerId = user.getStripeCustomerId();
        if(stripeCustomerId == null || stripeCustomerId.isEmpty()){
            throw new BadRequestException("User does not have a Stripe Customer Id, userId ,"+userId);
        }

        try {
            var   portalSession = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(stripeCustomerId)
                            .setReturnUrl(frontendUrl)
                            .build()
            );

            return new PortalResponse(portalSession.getUrl());
        }catch (StripeException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {

     log.info("Handling stripe event: {}",type);
     switch (type){
         case "checkout.session.complete" -> handleCheckoutSessionComplete((Session)stripeObject,metadata);
         case "customer.subscription.update" -> handleCustomerSubscriptionUpdate((Subscription)stripeObject);
         case "customer.subscription.delete" -> handleCustomerSubscriptionDelete((Subscription)stripeObject);
         case "invoice.paid" -> handleInvoicePaid((Invoice)stripeObject);
         case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice)stripeObject);
         default -> log.debug("Ignore the event : {} ",type);
     }


    }


    private  void handleCheckoutSessionComplete(Session session ,Map<String,String> metadata){

      if(session == null){
          log.error("Session object was null");
          return;
      }
      Long userId = Long.parseLong(metadata.get("userId"));
      Long planId = Long.parseLong(metadata.get("planId"));
      String subscriptionId = session.getSubscription();
      String customerId = session.getCustomer();

      User user = getUser(userId);
      if(user.getStripeCustomerId() == null){
          user.setStripeCustomerId(customerId);
          userRepository.save(user);
      }
      subscriptionService.activateSubscription(userId,planId,customerId,subscriptionId);

    }


    private void handleCustomerSubscriptionUpdate(Subscription subscription){
      if(subscription == null){
          log.error("Subscription object was null");
          return;
      }

      SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
      if(status == null){
          log.warn("Unknown status '{}' for subscription {}",subscription.getStatus(),subscription.getId());
      }

        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant periodStart = toInstant(item.getCurrentPeriodStart());
        Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

        Long planId = resolvePlanId(item.getPrice());
        subscriptionService.updateSubscription(subscription
                .getId(),status,periodStart,periodEnd,subscription
                .getCancelAtPeriodEnd(),planId);
    }


    private void handleCustomerSubscriptionDelete(Subscription subscription){
        if(subscription == null){
            log.error("Subscription object was null inside handleCustomerSubscriptionDeleted");
        }
        subscriptionService.cancelSubscription(subscription.getId());

    }


    private void handleInvoicePaid(Invoice invoice){
        String subId = extractSubscriptionId(invoice);
        if(subId == null)return;

        try{
            Subscription subscription = Subscription.retrieve(subId);
            var item = subscription.getItems().getData().get(0);

            Instant periodStart = toInstant(item.getCurrentPeriodStart());
            Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(subId,periodStart,periodEnd);

        }catch (StripeException e){
            throw new RuntimeException(e);
        }

    }


    private void handleInvoicePaymentFailed(Invoice invoice){

        String subId = extractSubscriptionId(invoice);
        if(subId == null)return;

        subscriptionService.markSubscriptionPostDue(subId);

    }

    // Util method ----->

    private User getUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("user",userId.toString()));
    }

    private SubscriptionStatus mapStripeStatusToEnum(String status){
        return switch (status){
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "post_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status:{}",status);
                yield null;
            }
        };
    }

    private Instant toInstant(Long epoch){
        return epoch != null ? Instant.ofEpochSecond(epoch):null;
    }
    private Long resolvePlanId(Price price){
        if(price == null || price.getId() == null) return  null;
        return planRepository.findByStripePriceId(price.getId())
                .map(Plan::getId)
                .orElse(null);
    }

    private String extractSubscriptionId(Invoice invoice){
        var parent =invoice.getParent();
        if(parent == null)return null;

        var subDetails = parent.getSubscriptionDetails();
        if(subDetails == null) return null;

        return  subDetails.getSubscription();
    }

}
