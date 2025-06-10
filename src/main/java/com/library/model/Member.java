package com.library.model;

// Lớp Member: Đại diện cho một thành viên trong thư viện
public class Member {
    private int id; // ID của thành viên
    private String username; // Tên đăng nhập của thành viên
    private String password; // Mật khẩu của thành viên
    private String fullName; // Họ và tên đầy đủ của thành viên
    private String role; // Vai trò của thành viên (ví dụ: "admin", "user")

    // Constructor: Khởi tạo một đối tượng Member với các thông tin cơ bản
    public Member(int id, String username, String password, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and setters
    // Lấy ID của thành viên
    public int getId() { return id; }
    // Đặt ID cho thành viên
    public void setId(int id) { this.id = id; }
    // Lấy tên đăng nhập
    public String getUsername() { return username; }
    // Đặt tên đăng nhập
    public void setUsername(String username) { this.username = username; }
    // Lấy mật khẩu
    public String getPassword() { return password; }
    // Đặt mật khẩu
    public void setPassword(String password) { this.password = password; }
    // Lấy họ và tên đầy đủ
    public String getFullName() { return fullName; }
    // Đặt họ và tên đầy đủ
    public void setFullName(String fullName) { this.fullName = fullName; }
    // Lấy vai trò
    public String getRole() { return role; }
    // Đặt vai trò
    public void setRole(String role) { this.role = role; }
}
