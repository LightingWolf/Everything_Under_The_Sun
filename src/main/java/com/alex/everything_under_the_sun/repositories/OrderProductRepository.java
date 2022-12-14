package com.alex.everything_under_the_sun.repositories;

import com.alex.everything_under_the_sun.model.OrderProduct;
import com.alex.everything_under_the_sun.model.OrderProductPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductPK> {
}
