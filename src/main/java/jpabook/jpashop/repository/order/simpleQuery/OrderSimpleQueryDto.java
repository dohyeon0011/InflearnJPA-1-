package jpabook.jpashop.repository.order.simpleQuery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
    public class OrderSimpleQueryDto { // 주문 목록을 보여주기 위한 DTO
    private Long orderId;
    private String name; // 주문자 이름
    private LocalDateTime orderDate; // 주문 시간
    private OrderStatus orderStatus; // 주문 상태
    private Address address; // 배송지 정보

    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }

}