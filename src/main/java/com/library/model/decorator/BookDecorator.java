package com.library.model.decorator;

import com.library.model.Book;

// Lớp trừu tượng BookDecorator: Cơ sở cho các Decorator của Book
// Nó đóng gói một đối tượng Book và ủy quyền các phương thức cho đối tượng đó.
public abstract class BookDecorator extends Book {
    protected Book decoratedBook; // Đối tượng Book được trang trí

    // Constructor: Khởi tạo decorator với một đối tượng Book cần trang trí
    public BookDecorator(Book decoratedBook) {
        super(decoratedBook.getId(), decoratedBook.getTitle(), decoratedBook.getAuthor());
        this.decoratedBook = decoratedBook;
    }

    @Override
    // Trả về loại của cuốn sách được trang trí
    public String getType() {
        return decoratedBook.getType();
    }

    @Override
    // Trả về ID của cuốn sách được trang trí
    public int getId() {
        return decoratedBook.getId();
    }

    @Override
    // Đặt ID cho cuốn sách được trang trí
    public void setId(int id) {
        decoratedBook.setId(id);
    }

    @Override
    // Trả về tiêu đề của cuốn sách được trang trí
    public String getTitle() {
        return decoratedBook.getTitle();
    }

    @Override
    // Đặt tiêu đề cho cuốn sách được trang trí
    public void setTitle(String title) {
        decoratedBook.setTitle(title);
    }

    @Override
    // Trả về tác giả của cuốn sách được trang trí
    public String getAuthor() {
        return decoratedBook.getAuthor();
    }

    @Override
    // Đặt tác giả cho cuốn sách được trang trí
    public void setAuthor(String author) {
        decoratedBook.setAuthor(author);
    }
}
