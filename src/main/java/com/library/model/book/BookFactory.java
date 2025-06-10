package com.library.model.book;

import com.library.model.Book;

// Giao diện factory để tạo các đối tượng Book
public interface BookFactory {
    // Phương thức tạo một đối tượng Book với ID, tiêu đề và tác giả
    Book createBook(int id, String title, String author);
}
