package jpabook.jpashop.repository.order.simpleQuery;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// 주문 조회 전용 리포지토리

/**
 * 쿼리 방식 선택 권장 순서**
 * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다. -> v2
 * 2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다. -> v3
 * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다. -> v4
 * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    // 주문 목록 반환용 최적화 select 쿼리문(join)
    // new operation을 사용해서 JPQL의 결과를 DTO로 즉시 반환
    // 단점 : 리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 직접 들어가는 단점(화면에 의존), 효과 미비
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.simpleQuery.OrderSimpleQueryDto(o.id, m.username, o.orderDate, o.status, d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }


}
