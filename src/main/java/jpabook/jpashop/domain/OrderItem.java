package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 객체 생성 막기
@Getter @Setter
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격

    private int count; // 주문 수량

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
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    // == 비즈니스 로직 == //
    /**
     * 주문 취소
     */
    public void cancel() {
        getItem().addStock(count); // 재고 수량 원복, JPA 변경 감지로 update 쿼리가 다 쫘르륵 날라감
    }

    // == 조회 로직 == //
    /**
     * 상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }

}
