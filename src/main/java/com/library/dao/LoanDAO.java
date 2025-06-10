package com.library.dao;

import com.library.config.DatabaseConfig;
import com.library.model.Loan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {
    // Thêm một bản ghi mượn sách mới vào cơ sở dữ liệu
    public void addLoan(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans (book_id, member_id, borrow_date, due_date, fee_strategy) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, loan.getBookId());
            stmt.setInt(2, loan.getMemberId());
            stmt.setDate(3, Date.valueOf(loan.getBorrowDate()));
            stmt.setDate(4, Date.valueOf(loan.getDueDate()));
            stmt.setString(5, loan.getFeeStrategy() != null ? loan.getFeeStrategy() : "daily"); // Mặc định
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    loan.setId(rs.getInt(1));
                }
            }
        }
    }

    // Lấy tất cả các bản ghi mượn sách từ cơ sở dữ liệu
    public List<Loan> getAllLoans() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans";
        System.out.println("Đang lấy danh sách loans từ DB...");
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Loan loan = new Loan();
                loan.setId(rs.getInt("id"));
                loan.setBookId(rs.getInt("book_id"));
                loan.setMemberId(rs.getInt("member_id"));
                loan.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                loan.setDueDate(rs.getDate("due_date").toLocalDate());
                Date returnDate = rs.getDate("return_date");
                loan.setReturnDate(returnDate != null ? returnDate.toLocalDate() : null);
                loan.setOverdueFee(rs.getDouble("overdue_fee"));
                loan.setFeeStrategy(rs.getString("fee_strategy")); // Lấy chiến lược từ DB
                loans.add(loan);
            }
            System.out.println("Số lượng loans lấy được: " + loans.size());
            System.out.println("Số lượng sách đang mượn (return_date IS NULL): " +
                    loans.stream().filter(loan -> loan.getReturnDate() == null).count());
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách loans: " + e.getMessage());
            throw e;
        }
        return loans;
    }

    // Lấy một bản ghi mượn sách theo ID từ cơ sở dữ liệu
    public Loan getLoanById(int id) throws SQLException {
        String sql = "SELECT * FROM loans WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Loan loan = new Loan();
                    loan.setId(rs.getInt("id"));
                    loan.setBookId(rs.getInt("book_id"));
                    loan.setMemberId(rs.getInt("member_id"));
                    loan.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                    loan.setDueDate(rs.getDate("due_date").toLocalDate());
                    Date returnDate = rs.getDate("return_date");
                    loan.setReturnDate(returnDate != null ? returnDate.toLocalDate() : null);
                    loan.setOverdueFee(rs.getDouble("overdue_fee"));
                    loan.setFeeStrategy(rs.getString("fee_strategy"));
                    return loan;
                }
            }
        }
        return null;
    }

    // Lấy danh sách các bản ghi mượn sách của một thành viên
    public List<Loan> getLoansByMember(int memberId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE member_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Loan loan = new Loan();
                    loan.setId(rs.getInt("id"));
                    loan.setBookId(rs.getInt("book_id"));
                    loan.setMemberId(rs.getInt("member_id"));
                    loan.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                    loan.setDueDate(rs.getDate("due_date").toLocalDate());
                    Date returnDate = rs.getDate("return_date");
                    loan.setReturnDate(returnDate != null ? returnDate.toLocalDate() : null);
                    loan.setOverdueFee(rs.getDouble("overdue_fee")); // Đảm bảo lấy từ DB
                    loan.setFeeStrategy(rs.getString("fee_strategy"));
                    loans.add(loan);
                }
            }
        }
        return loans;
    }

    // Cập nhật thông tin bản ghi mượn sách (ví dụ: ngày trả, phí quá hạn)
    public void updateLoan(Loan loan) throws SQLException {
        String sql = "UPDATE loans SET return_date = ?, overdue_fee = ?, fee_strategy = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, loan.getReturnDate() != null ? Date.valueOf(loan.getReturnDate()) : null);
            stmt.setDouble(2, loan.getOverdueFee());
            stmt.setString(3, loan.getFeeStrategy());
            stmt.setInt(4, loan.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Không có hàng nào được cập nhật cho loan id: " + loan.getId());
            } else {
                System.out.println("Cập nhật thành công loan id: " + loan.getId());
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật loan: " + e.getMessage());
            throw e;
        }
    }
}