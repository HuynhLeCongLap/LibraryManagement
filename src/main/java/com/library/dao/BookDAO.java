package com.library.dao;

import com.library.config.DatabaseConfig;
import com.library.model.Book;
import com.library.model.book.AcademicBookFactory;
import com.library.model.book.EntertainmentBookFactory;
import com.library.model.book.BookFactory;
import com.library.model.decorator.FavoriteBookDecorator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    // Thêm một cuốn sách mới vào cơ sở dữ liệu
    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getType());
            stmt.executeUpdate();
        }
    }

    // Lấy tất cả sách từ cơ sở dữ liệu
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BookFactory factory = rs.getString("type").equals("Printed") ? new AcademicBookFactory() : new EntertainmentBookFactory();
                Book book = factory.createBook(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author")
                );
                if (rs.getBoolean("is_favorite")) {
                    book = new FavoriteBookDecorator(book);
                }
                books.add(book);
            }
        }
        return books;
    }

    // Lấy sách theo ID từ cơ sở dữ liệu
    public Book getBookById(int id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BookFactory factory = rs.getString("type").equals("Printed") ? new AcademicBookFactory() : new EntertainmentBookFactory();
                Book book = factory.createBook(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author")
                );
                if (rs.getBoolean("is_favorite")) {
                    book = new FavoriteBookDecorator(book);
                }
                return book;
            }
        }
        return null;
    }

    // Cập nhật thông tin sách trong cơ sở dữ liệu
    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, type = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getType());
            stmt.setInt(4, book.getId());
            stmt.executeUpdate();
        }
    }

    // Xóa sách theo ID và các bản ghi liên quan (ví dụ: mượn sách)
    public void deleteBook(int id) throws SQLException {
        String deleteLoansSql = "DELETE FROM loans WHERE book_id = ?";
        String deleteBookSql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement deleteLoansStmt = conn.prepareStatement(deleteLoansSql);
             PreparedStatement deleteBookStmt = conn.prepareStatement(deleteBookSql)) {
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            deleteLoansStmt.setInt(1, id);
            deleteLoansStmt.executeUpdate();

            deleteBookStmt.setInt(1, id);
            deleteBookStmt.executeUpdate();

            conn.commit(); // Hoàn tất giao dịch
        } catch (SQLException e) {
            // Ném lại ngoại lệ với thông báo rõ ràng hơn
            throw new SQLException("Loi khi xoa sach: " + e.getMessage(), e);
        }
    }

    // Đánh dấu sách là yêu thích
    public void markAsFavorite(int id) throws SQLException {
        String sql = "UPDATE books SET is_favorite = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Bỏ đánh dấu sách là yêu thích
    public void unmarkAsFavorite(int id) throws SQLException {
        String sql = "UPDATE books SET is_favorite = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Lấy danh sách các sách đang có sẵn (chưa được mượn)
    public List<Book> getAvailableBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.* FROM books b LEFT JOIN loans l ON b.id = l.book_id AND l.return_date IS NULL WHERE l.book_id IS NULL";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BookFactory factory = rs.getString("type").equals("Printed") ? new AcademicBookFactory() : new EntertainmentBookFactory();
                Book book = factory.createBook(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author")
                );
                if (rs.getBoolean("is_favorite")) {
                    book = new FavoriteBookDecorator(book);
                }
                books.add(book);
            }
        }
        return books;
    }

    // Tìm kiếm sách dựa trên các tiêu chí (ID, tiêu đề, tác giả, thể loại, yêu thích)
    public List<Book> searchBooks(String idStr, String title, String author, String type, String favoriteStr) throws SQLException {
        List<Book> books = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (idStr != null && !idStr.isEmpty()) {
            sql.append(" AND id = ?");
            params.add(Integer.parseInt(idStr));
        }

        if (title != null && !title.isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + title + "%");
        }

        if (author != null && !author.isEmpty()) {
            sql.append(" AND author LIKE ?");
            params.add("%" + author + "%");
        }

        if (type != null && !type.isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type);
        }

        if (favoriteStr != null && !favoriteStr.isEmpty()) {
            sql.append(" AND is_favorite = ?");
            params.add(Boolean.parseBoolean(favoriteStr));
        }

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
            }
        }

        return books;
    }

    // Phương thức trợ giúp để tạo đối tượng Book từ ResultSet
    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String author = rs.getString("author");
        String type = rs.getString("type");
        boolean isFavorite = rs.getBoolean("is_favorite");

        BookFactory factory = type.equals("Printed") ? new AcademicBookFactory() : new EntertainmentBookFactory();
        Book book = factory.createBook(id, title, author);
        if (isFavorite) {
            book = new FavoriteBookDecorator(book);
        }
        return book;
    }
}
