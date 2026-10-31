package com.smartcanteen.repository;

import com.smartcanteen.model.Order;
import com.smartcanteen.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    List<Order> findByCustomerRollNumberIgnoreCaseOrderByCreatedAtDesc(String customerRollNumber);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);

    // Count how many orders with given statuses were placed BEFORE a specific timestamp
    long countByStatusInAndCreatedAtBefore(List<OrderStatus> statuses, LocalDateTime createdAt);

    // Retrieve active orders placed before a specific timestamp to calculate ahead queue preparation time
    List<Order> findByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(List<OrderStatus> statuses, LocalDateTime createdAt);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN (:statuses)")
    long countActiveOrders(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal calculateTotalGrossRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED'")
    long countCompletedOrders();
}
