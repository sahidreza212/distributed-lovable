package com.sarm.distributed_lovable.account_service.repository;


import com.sarm.distributed_lovable.account_service.entity.Subscription;
import com.sarm.distributed_lovable.common_lib.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {


    boolean existsByStripeSubscriptionId(String subscriptionId);

   // void save(com.sarm.project.Lovable_Clone.entitys.Subscription subscription);
    Optional<Subscription> findByStripeSubscriptionId(String gatewaySubscriptionId);

    Optional<Subscription> findByUserIdAndStatusIn(Long userId, Set<SubscriptionStatus> statusSet);
}
