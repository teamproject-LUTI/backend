package com.luti.luti.payment.repository;

import com.luti.luti.payment.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

}
