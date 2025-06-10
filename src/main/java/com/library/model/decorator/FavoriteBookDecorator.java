package com.library.model.decorator;

import com.library.dao.BookDAO;
import com.library.model.Book;

import java.sql.SQLException;

// Lớp FavoriteBookDecorator: Trang trí một cuốn sách để thêm chức năng yêu thích
public class FavoriteBookDecorator extends BookDecorator {
    // Constructor: Khởi tạo decorator với cuốn sách cần trang trí
    public FavoriteBookDecorator(Book decoratedBook) {
        super(decoratedBook);
    }

    @Override
    // Ghi đè phương thức getTitle để thêm hậu tố "[Yêu thích]"
    public String getTitle() {
        return decoratedBook.getTitle() + " [Yêu thích]";
    }

    // Đánh dấu cuốn sách được trang trí là yêu thích trong cơ sở dữ liệu
    public String markAsFavorite() {
        try {
            BookDAO bookDAO = new BookDAO();
            bookDAO.markAsFavorite(decoratedBook.getId());
            return "Book '" + decoratedBook.getTitle() + "' has been marked as favorite.";
        } catch (SQLException e) {
            return "Error marking book as favorite: " + e.getMessage();
        }
    }

    // Bỏ đánh dấu cuốn sách được trang trí là yêu thích trong cơ sở dữ liệu
    public String unmarkAsFavorite() {
        try {
            BookDAO bookDAO = new BookDAO();
            bookDAO.unmarkAsFavorite(decoratedBook.getId());
            return "Book '" + decoratedBook.getTitle() + "' has been unmarked as favorite.";
        } catch (SQLException e) {
            return "Error unmarking book as favorite: " + e.getMessage();
        }
    }
}
