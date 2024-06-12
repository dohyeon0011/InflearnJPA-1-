package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.dto.BookForm;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book book = (Book) itemService.findById(itemId);

        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setName(book.getName());
        form.setPrice(book.getPrice());
        form.setStockQuantity(book.getStockQuantity());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {
        // 한번 데이터베이스에 갔다 온(DB에 이미 한 번 저장된) 객체를 준영속성 상태의 엔티티 라고 함.
        // 준영속 엔티티는 JPA가 관리하지 않아 데이터 변경 불가능.
        // 해결법 : 1. 변경 감지 기능 사용 -> 이거로 써야함.
        //        2. 병합(merge) 사용 -> 모든 필드 값이 변경 되는 치명적 단점.(만약 하나의 필드를 그냥 안 넣으면 null로 들어감)
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

        return "redirect:/items";
    }

    /*@PostMapping("items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") BookForm form) {

        // 한번 데이터베이스에 갔다 온(DB에 이미 한 번 저장된) 객체를 준영속성 상태의 엔티티 라고 함.
        // 준영속 엔티티는 JPA가 관리하지 않아 데이터 변경 불가능.
        // 해결법 : 1. 변경 감지 기능 사용 -> 이거로 써야함.
        //        2. 병합(merge) 사용 -> 모든 필드 값이 변경 되는 치명적 단점.(만약 하나의 필드를 그냥 안 넣으면 null로 들어감)
        *//*Book book = new Book();
        book.setId(form.getId());
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book); // 수정 시도*//*

        return "redirect:/items";
    }*/

}
