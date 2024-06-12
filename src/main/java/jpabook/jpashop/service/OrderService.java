package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 핵심 비즈니스 로직들을 엔티티에 몰아 넣고, 서비스에서는 엔티티에 필요한 요청만을 호출하는 패턴을 도메인 모델 패턴이라고 한다.(보통 이 패턴으로 함)
// 도메인 패턴은 엔티티에 대한 테스트 코드를 작성하기 좋음.
// 서비스에서 비즈니스 로직을 처리하는 패턴은 트랜잭션 스크립트 패턴이라함.
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     * cascade 옵션 덕분에 따로 DeliveryRepository를 만들지 않고, orderRepository에만 persist 날려도 영속성 전이됨.
     * 트랜잭션 한 단위에서 핵심 비즈니스 로직을 수행해야 영속 상태에서 하는 거라 값을 변경해도 변경 감지가 됨.
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = memberRepository.findById(memberId);
        Item item = itemRepository.findById(itemId);

        // 배송 정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 주문 상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);
        order.cancel();
    }

    /**
     * 검색
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findByAllString(orderSearch);
    }
}
