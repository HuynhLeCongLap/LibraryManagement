package com.library.model.book;

import com.library.model.Book;

// Lớp đại diện cho một loại sách in (PrintedBook)
public class PrintedBook extends Book {
    // Constructor để tạo một đối tượng PrintedBook
    public PrintedBook(int id, String title, String author) {
        super(id, title, author);
    }

    @Override
    // Trả về loại sách là "Printed"
    public String getType() {
        return "Printed";
    }
}
