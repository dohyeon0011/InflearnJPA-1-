package jpabook.jpashop.service;


import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional // flush() 날려서 디비에 update 쿼리침.(변경 감지, 이게 나은 방법)
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        Item findItem = itemRepository.findById(itemId);
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
    }

    /*@Transactional // flush() 날려서 디비에 update 쿼리침.(변경 감지, 이게 나은 방법)
    public void updateItem(Long itemId, UpdateItemDto itemDto) {
        Item findItem = itemRepository.findById(itemId);
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
    }*/

    /*@Transactional // flush() 날려서 디비에 update 쿼리침.(변경 감지, 이게 나은 방법)
    public Item updateItem(Long itemId, Book book) {
        Item findItem = itemRepository.findById(itemId);
        findItem.setName(book.getName());
        findItem.setPrice(book.getPrice());
        findItem.setStockQuantity(book.getStockQuantity());

        return findItem;
    }*/

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findById(Long itemId) {
        return itemRepository.findById(itemId);
    }

}
