package com.library.model.book;

import com.library.model.Book;

// Lớp đại diện cho một loại sách điện tử (EBook)
public class EBook extends Book {
    // Constructor để tạo một đối tượng EBook
    public EBook(int id, String title, String author) {
        super(id, title, author);
    }

    @Override
    // Trả về loại sách là "EBook"
    public String getType() {
        return "EBook";
    }
}
