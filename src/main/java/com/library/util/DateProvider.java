package com.library.util;

import java.time.LocalDate;

// Lớp DateProvider: Cung cấp ngày hiện tại, hỗ trợ mocking cho mục đích kiểm thử
public class DateProvider {
    private static DateProvider instance; // Thể hiện duy nhất của DateProvider (Singleton)
    private LocalDate mockDate; // Ngày giả lập cho mục đích kiểm thử

    // Constructor riêng tư: Đảm bảo chỉ có một thể hiện duy nhất
    private DateProvider() {
        this.mockDate = null; // Khởi tạo mockDate là null, tức là sử dụng ngày hiện tại mặc định
    }

    // Lấy thể hiện duy nhất của DateProvider (Singleton Pattern)
    public static synchronized DateProvider getInstance() {
        if (instance == null) {
            instance = new DateProvider();
        }
        return instance;
    }

    // Lấy ngày hiện tại: Trả về ngày giả lập nếu có, ngược lại trả về ngày thực tế
    public LocalDate getCurrentDate() {
        return (mockDate != null) ? mockDate : LocalDate.now();
    }

    // Đặt ngày giả lập cho mục đích kiểm thử
    public void setMockDate(LocalDate mockDate) {
        this.mockDate = mockDate;
    }

    // Xóa ngày giả lập, quay về sử dụng ngày hiện tại thực tế
    public void clearMockDate() {
        this.mockDate = null;
    }
}
