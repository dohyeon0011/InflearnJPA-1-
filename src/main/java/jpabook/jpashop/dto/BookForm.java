package jpabook.jpashop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookForm {

    private Long id; // 상품 수정 때문에 받음.

    private int price;
    private String name;
    private int stockQuantity;

    private String author;
    private String isbn;

}
