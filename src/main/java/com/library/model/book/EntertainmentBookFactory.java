package com.library.model.book;

import com.library.model.Book;

// Factory để tạo các đối tượng sách giải trí
public class EntertainmentBookFactory implements BookFactory {
    @Override
    // Tạo một đối tượng sách giải trí (ví dụ: EBook)
    public Book createBook(int id, String title, String author) {
        return new EBook(id, title, author); // Ví dụ: Sách giải trí là sách điện tử
    }
    private static class EntertainmentBook extends Book {
        public EntertainmentBook(int id, String title, String author) {
            super(id, title, author);
        }

        @Override
        public String getType() {
            return "Entertainment";
        }
    }
}
