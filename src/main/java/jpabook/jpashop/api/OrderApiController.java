package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * API 조회 권장 순서**
 * 1. 엔티티 조회 방식으로 우선 접근
 *      -1 페치조인으로 쿼리 수를 최적화
 *      -2 컬렉션 최적화
 *          -1. 페이징 필요 `hibernate.default_batch_fetch_size` , `@BatchSize` 로 최적화
 *          -2. 페이징 필요X 페치 조인 사용
 * 2. 엔티티 조회 방식으로 해결이 안 되면 DTO 조회 방식 사용
 * 3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate
 * 참고: 엔티티 조회 방식은 페치 조인이나, hibernate.default_batch_fetch_size, @BatchSize 같이 코드를 거의 수정하지 않고, 옵션만 약간 변경해서,
 * 다양한 성능 최적화를 시도할 수 있다. 반면에 DTO를 직접 조회하는 방식은 성능을 최적화 하거나 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 한다.
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;


    /**
     * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1 Query) (1 Query)
     *      플랫 데이터 최적화 - JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환
     * 한방 쿼리, 플랫 데이터 최적화
     * Query: 1번
     * 단점
     * - 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5 보다 더 느릴 수도 있다.
     * - 애플리케이션에서 추가 작업이 크다.
     * - 페이징 불가능 (OrderItems 기준 페이징 가능, Order 기준 페이징 불가, JPA상으로는 gropingBy로 orderId를 묶어 데이터 뻥튀기를 막았지만 디비 쿼리는 뻥튀기 되어 있음.)
     * -> 왜냐? Order에서 2개만 가져와라 하면 각기 다른 orderId를 가진 2개를 가져와야 하는데 같은 orderId = 1을 2개 가져와버림.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_falt();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList());
//        return flats;
    }

    /**
     * V5. 컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 IN 절을 활용해서 메모리에 미리 조회해서 최적화
     *      JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
     *  페이징 가능
     *  쿼리 두방
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
     * 일대다(컬렉션) DTO 직접 조회
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * V3.1 컬렉션 페이징과 한계 돌파
     * ToOne 관계는 X대다 데이터 뻥튀기가 발생하지 않으니 이땐 fetch join 하고 파라미터로 가져올 데이터 수 페이징
     * yml에서 default_batch_fetch_size 로 인 쿼리 글로벌 설정
     * 1.장점
     *      쿼리 호출 수가 1 + N -> 1 + 1 로 최적화 된다.
     *      조인보다 DB 데이터 전송량이 최적화 된다. (Order와 OrderItem을 조인하면 Order가 OrderItem 만큼 중복해서 조회된다. 이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없다.)
     *      페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소한다.
     *      컬렉션 페치 조인은 페이징이 불가능 하지만 이 방법은 페이징이 가능하다.
     * 2.결론
     *      ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다.
     *      따라서 ToOne 관계는 페치조인으로 쿼리 수 를 줄이고 해결하고, 나머지는 hibernate.default_batch_fetch_size 로 최적화 하자.
     * #참고: default_batch_fetch_size 의 크기는 적당한 사이즈를 골라야 하는데, 100~1000 사이를 선택하는 것을 권장한다.
     *      이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절 파라미터를 1000으로 제한하기도 한다.
     *      1000으로 잡으면 한번에 1000개를 DB에서 애플리케이션에 불러오므로 DB에 순간 부하가 증가할 수 있다.
     *      하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량이 같다.
     *      1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션이든 순간 부하를 어디까지 견딜 수 있는 지로 결정하면 된다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_paging(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                          @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // orderItems를 컬렉션만큼 batch_fetch_size 개수만큼 한 번에 IN 쿼리로 땡겨옴
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * V3. 페치 조인으로 쿼리 수 최적화
     * 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * V2
     *  엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 트랜잭션 안에서 지연 로딩 필요
     * SQL 실행 수(지연 로딩 때문에)
     * order 1번
     * member, address N번(order 조회 수 만큼), orderItem N번(order 조회 수 만큼)
     * item N번(orderItem 조회 수 만큼)
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findByAllString(new OrderSearch());
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * V1
     * 엔티티 직접 노출 (사용 X)
     *  - 엔티티가 변하면 API 스펙이 변한다.
     *  - 트랜잭션 안에서 지연 로딩 필요
     *  - 양방향 연관관계 문제
     *  order -> orderItems 일대다 컬렉션 조회
     *  오더 엔티티 자체를 리턴해서 int getTotal()까지 다 나옴.
     *  - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findByAllString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getUsername();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); // 주문된 아이템에서 아이템 이름도 조회하고 싶을 때.
        }
        return all;
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // DTO 안에 엔티티 있으면 안됨.(외부에 노출되고, 엔티티에 대한 의존도가 높아짐, 이것 조차도 DTO로 반환해야 함.)

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getUsername();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
//            order.getOrderItems().stream().forEach(o -> o.getItem().getName()); // orderItem은 엔티티라 지연로딩 걸려 있어서 초기화 해줘야 함.
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new) // 이 시점에 프록시 말고 실제 객체 데이터 가져와서 select 나감
                    .collect(toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }

    }
}
