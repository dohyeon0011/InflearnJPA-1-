package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne(ManyToOne, OntToOne) 조회 성능 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

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
        List<Order> all = orderRepository.findByAllString(new OrderSearch());
        for (Order order : all) { // 이러면 멤버 객체 필드와 딜리버리 객체 필드 정보를 다 가져옴, 얘 없으면 딱 Order 엔티티 클래스의 있는 정보 그대로만 가져옴.
            order.getMember().getUsername(); // getMember() 까지는 프록시지만 getUsername() 부터는 실제 객체 데이터 값을 가져옴.(member의 username 실제 값을 가져와서, Lazy 강제 초기화)
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }

}
