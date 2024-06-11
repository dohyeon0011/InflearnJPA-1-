package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) { // 아이템은 JPA에 저장하기 전까지 id값이 없음. 없다는 뜻은 완전 새롭게 생성한 객체라는 뜻.(신규 등록이다), 아이디가 있으면 이미 db에 있다는 것.
            em.persist(item);
        } else {
            em.merge(item); // 강제 업데이트?
        }
    }

    public Item findById(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from item i", Item.class)
                .getResultList();
    }
}
