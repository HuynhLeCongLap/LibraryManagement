package com.library.manager;

public interface Observer {
    /**
     * Phương thức này được gọi khi có một cập nhật mới cần thông báo.
     *
     * @param message Chuỗi tin nhắn mô tả cập nhật.
     */
    void update(String message);
}
