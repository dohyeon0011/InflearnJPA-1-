package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.order.simpleQuery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simpleQuery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OntToOne) 조회 성능 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
// 조회 시 orderItem 포함 X
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // 최적화 select(join) + 응답용 DTO
    // 보고 싶은 정보만을 select
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> oversV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    // fetch join 최적화(한방 쿼리), 근데 엔티티의 모든 필드를 다 끌어와서 최적화가 덜 됨.
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> oversV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> oversV2() {
        // ORDER 2개 조회 됨.
        // N + 1 -> 1 + N(회원) + N(배송) : 하나의 오더안에 멤버를 조회하고, 배송을 또 조회.
        // LAZY는 영속성 컨텍스트를 찔러보고 없으면 디비에 쿼리 날리기 때문에 전에 같은 유저를 조회한 전력이 있으면 디비 쿼리 안 날림.
        List<Order> orders = orderRepository.findByAllString(new OrderSearch());

        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    // 이러면 오더에서 멤버 갔다가 멤버에서 다시 오더가고 이렇게 무한 루프돔.
    /**
     * 엔티티를 직접 노출하는 것은 좋지 않다. (앞장에서 이미 설명)
     * order, member 와 order delivery 는 지연로딩이다.
     * 따라서 실제 엔티티 대신에 프록시 존재
     * jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모름.
     * 예외 발생 `Hibernate5Module` 을 스프링 빈으로 등록하면 해결(스프링 부트 사용중)
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findByAllString(new OrderSearch()); // 아래 for-each문으로 프록시 객체가 아닌 실 객체 데이터를 가져오기 전 까지는 null 값이 뜰 거임. LAZY모드라 프록시라서
//        for (Order order : all) { // 이러면 멤버 객체 필드와 딜리버리 객체 필드 정보를 다 가져옴, 얘 없으면 딱 Order 엔티티 클래스의 있는 정보 그대로만 가져옴.
//            order.getMember().getUsername(); // getMember() 까지는 프록시지만 getUsername() 부터는 실제 객체 데이터 값을 가져옴.(member의 username 실제 값을 가져와서, Lazy 강제 초기화)
//            order.getDelivery().getAddress(); // Lazy 강제 초기화
//        }
        return all;
    }

    @Data
    static class SimpleOrderDto { // 주문 목록을 보여주기 위한 DTO
        private Long orderId;
        private String name; // 주문자 이름
        private LocalDateTime orderDate; // 주문 시간
        private OrderStatus orderStatus; // 주문 상태
        private Address address; // 배송지 정보

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getUsername();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

}
