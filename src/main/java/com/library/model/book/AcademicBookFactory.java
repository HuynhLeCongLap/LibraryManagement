package com.library.model.book;

import com.library.model.Book;

// Factory để tạo các đối tượng sách học thuật
public class AcademicBookFactory implements BookFactory {
    @Override
    // Tạo một đối tượng sách học thuật (ví dụ: PrintedBook)
    public Book createBook(int id, String title, String author) {
        return new PrintedBook(id, title, author); // Ví dụ: Sách học thuật là sách in
    }
    private static class AcademicBook extends Book {
        public AcademicBook(int id, String title, String author) {
            super(id, title, author);
        }

        @Override
        public String getType() {
            return "Printed";
        }
    }
}
