package com.library.model;

import com.library.model.strategy.FeeCalculator;
import com.library.model.strategy.DailyFeeCalculator;
import com.library.model.strategy.QuantityFeeCalculator;
import com.library.model.strategy.FeeContext;

import java.time.LocalDate;

// Lớp Loan: Đại diện cho một bản ghi mượn sách
public class Loan {
    private int id; // ID của bản ghi mượn
    private int bookId; // ID của sách được mượn
    private int memberId; // ID của thành viên mượn sách
    private LocalDate borrowDate; // Ngày mượn sách
    private LocalDate dueDate; // Ngày đến hạn trả sách
    private LocalDate returnDate; // Ngày thực tế trả sách
    private double overdueFee; // Phí quá hạn (nếu có)
    private Book book; // Đối tượng sách liên quan
    private Member member; // Đối tượng thành viên liên quan
    private FeeContext feeContext; // Đối tượng quản lý chiến lược tính phí
    private String feeStrategy; // Chiến lược tính phí hiện tại (để đồng bộ với DB)

    // Constructor mặc định: Khởi tạo với chiến lược tính phí hàng ngày
    public Loan() {
        this.feeContext = new FeeContext(new DailyFeeCalculator()); // Mặc định là DailyFeeCalculator
        this.feeStrategy = "daily"; // Mặc định
    }

    // Getters and setters
    // Lấy ID của bản ghi mượn
    public int getId() { return id; }
    // Đặt ID cho bản ghi mượn
    public void setId(int id) { this.id = id; }
    // Lấy ID sách
    public int getBookId() { return bookId; }
    // Đặt ID sách
    public void setBookId(int bookId) { this.bookId = bookId; }
    // Lấy ID thành viên
    public int getMemberId() { return memberId; }
    // Đặt ID thành viên
    public void setMemberId(int memberId) { this.memberId = memberId; }
    // Lấy ngày mượn
    public LocalDate getBorrowDate() { return borrowDate; }
    // Đặt ngày mượn
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    // Lấy ngày đến hạn
    public LocalDate getDueDate() { return dueDate; }
    // Đặt ngày đến hạn
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    // Lấy ngày trả
    public LocalDate getReturnDate() { return returnDate; }
    // Đặt ngày trả và tính phí quá hạn nếu có
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
        if (returnDate != null && dueDate != null) {
            calculateOverdueFee(); // Tính phí khi trả sách
        }
    }
    // Lấy phí quá hạn
    public double getOverdueFee() { return overdueFee; }
    // Đặt phí quá hạn
    public void setOverdueFee(double overdueFee) { this.overdueFee = overdueFee; }
    // Lấy đối tượng sách
    public Book getBook() { return book; }
    // Đặt đối tượng sách
    public void setBook(Book book) { this.book = book; }
    // Lấy đối tượng thành viên
    public Member getMember() { return member; }
    // Đặt đối tượng thành viên
    public void setMember(Member member) { this.member = member; }
    // Lấy chiến lược tính phí
    public String getFeeStrategy() { return feeStrategy; }

    // Phương thức tính toán phí trễ hạn dựa trên chiến lược hiện tại
    public void calculateOverdueFee() {
        if (feeContext != null) {
            this.overdueFee = feeContext.calculateFee(this);
        } else {
            this.overdueFee = 0.0;
        }
    }

    // Setter để chọn và cập nhật chiến lược tính phí
    public void setFeeStrategy(String feeType) {
        this.feeStrategy = feeType != null ? feeType.toLowerCase() : "daily";
        FeeCalculator calculator;
        switch (this.feeStrategy) {
            case "daily":
                calculator = new DailyFeeCalculator();
                break;
            case "quantity":
                calculator = new QuantityFeeCalculator();
                break;
            default:
                calculator = new DailyFeeCalculator(); // Mặc định nếu không khớp
                break;
        }
        this.feeContext.setFeeCalculator(calculator);
        calculateOverdueFee(); // Tính lại phí ngay sau khi thay đổi chiến lược
    }
}
