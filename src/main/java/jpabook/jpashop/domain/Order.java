package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 객체 생성 막기
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태(ORDER, CANCEL)

    // 연관관계 편의 메서드(주 엔티티에)
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    // == 생성 메서드 == //
    /**
     * static으로 선언한 이유.
     * 1.생성자만을 사용할 때보다 더 명확한 이름을 제공할 수 있다. 예를 들어, valueOf, of, getInstance, newInstance, getType 등의 명명 규칙을 사용하여 메서드 의도를 더 명확하게 표현할 수 있다.
     * 2.호출될 때마다 새로운 객체를 생성하지 않아도 된다. 즉, 불필요한 객체 생성을 피하고 메모리 사용을 최적화할 수 있으며, 필요한 경우 미리 생성된 인스턴스를 반환할 수 있다.
     * 3.반환 타입의 하위 타입 객체를 반환할 수 있다. 이를 통해 구현 클래스를 숨기는 캡슐화를 달성할 수 있어 API를 더 간결하게 유지할 수 있다.
     * 4.입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있습니다. 이는 유연성을 제공하며 디자인 패턴 중 Factory Pattern의 적용 사례 중 하나.
     * =========================================================================================================================================================================
     * 주의점
     * 다만, static 메서드로 정의된 팩토리 메소드를 사용할 경우 생성자와 달리 상속이 불가능하다는 점에 유의해야 한다.
     * static을 빼고 일반 메서드로 변경할 경우, 해당 메서드를 사용하기 위해서는 먼저 객체의 인스턴스를 생성해야 하므로,
     * 해당 클래스의 다른 static 메서드들이나 생성자에서는 사용할 수 없게 된다. 따라서, 클래스의 인스턴스 없이 호출 가능하게 하려면
     * static을 사용하여 정적 메서드로 선언해야 한다.
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER); // JPA 변경 감지로 update 쿼리가 다 쫘르륵 날라감
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    // == 비즈니스 로직 == //
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);

        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    // == 조회 로직 == //
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        /*int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice = orderItem.getTotalPrice();
        }
        return totalPrice;*/

        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

}
