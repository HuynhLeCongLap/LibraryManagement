create database library;
use library;
CREATE TABLE books (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       type VARCHAR(50) NOT NULL,
                       is_favorite BOOLEAN DEFAULT FALSE
);

CREATE TABLE members (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         username VARCHAR(50) UNIQUE NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         full_name VARCHAR(255) NOT NULL,
                         role ENUM('ADMIN', 'MEMBER') NOT NULL
);

CREATE TABLE loans (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       book_id INT NOT NULL,
                       member_id INT NOT NULL,
                       borrow_date DATE NOT NULL,
                       return_date DATE,
                       due_date DATE NOT NULL,
                       overdue_fee DECIMAL(10,2) DEFAULT 0.0,
                       FOREIGN KEY (book_id) REFERENCES books(id),
                       FOREIGN KEY (member_id) REFERENCES members(id)
);

-- Insert default admin account
INSERT INTO members (username, password, full_name, role)
VALUES ('admin', 'admin123', 'Administrator', 'ADMIN');

ALTER TABLE loans
DROP FOREIGN KEY loans_ibfk_1;

ALTER TABLE loans
    ADD CONSTRAINT loans_ibfk_1
        FOREIGN KEY (book_id)
            REFERENCES books (id)
            ON DELETE CASCADE;

ALTER TABLE loans ADD COLUMN fee_strategy VARCHAR(20);

INSERT INTO books (id, title, author, type, is_favorite) VALUES
                                                             (1, 'Hay', 'L', 'Printed', FALSE),
                                                             (2, 'Tuổi trẻ đáng giá bao nhiêu?', 'Rosie Nguyễn', 'Printed', TRUE),
                                                             (3, 'Đắc nhân tâm', 'Dale Carnegie', 'Printed', FALSE),
                                                             (4, 'Tôi thấy hoa vàng trên cỏ xanh', 'Nguyễn Nhật Ánh', 'Printed', TRUE),
                                                             (5, 'Cho tôi xin một vé đi tuổi thơ', 'Nguyễn Nhật Ánh', 'Printed', TRUE),
                                                             (6, 'Người Nam Châm - Bí mật của luật hấp dẫn', 'Jack Canfield', 'Printed', FALSE),
                                                             (7, 'Quẳng gánh lo đi và vui sống', 'Dale Carnegie', 'Printed', FALSE),
                                                             (8, 'Bên thắng cuộc', 'Huy Đức', 'Printed', FALSE),
                                                             (9, 'Số đỏ', 'Vũ Trọng Phụng', 'Printed', TRUE),
                                                             (10, 'Tắt đèn', 'Ngô Tất Tố', 'Printed', TRUE),
                                                             (11, 'Lão Hạc', 'Nam Cao', 'Printed', FALSE),
                                                             (12, 'Sách Mẫu Printed', 'Tác giả A', 'Printed', FALSE),
                                                             (13, 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 'EBook', TRUE),
                                                             (14, 'Atomic Habits', 'James Clear', 'EBook', FALSE),
                                                             (15, 'Spring Boot Guide', 'Craig Walls', 'EBook', TRUE),
                                                             (16, 'Effective Java', 'Joshua Bloch', 'EBook', FALSE);

-- Thêm dữ liệu mẫu
INSERT INTO loans (book_id, member_id, borrow_date, due_date, return_date, fee_strategy)
VALUES
    (9, 2, '2025-05-20', '2025-05-26', NULL, 'daily'),    -- Hạn trả 26/05, chưa trả, trễ 1 ngày (tính đến 27/05/2025)
    (10, 10, '2025-05-20', '2025-05-26', NULL, 'quantity'), -- Hạn trả 26/05, chưa trả, trễ 1 ngày
    (11, 12, '2025-05-20', '2025-05-27', NULL, 'daily');   -- Hạn trả 27/05, chưa trả, chưa trễ

-- Tính phí trễ hạn
UPDATE loans
SET overdue_fee = CASE
                      WHEN fee_strategy = 'daily' THEN
                          GREATEST(0, DATEDIFF(COALESCE(return_date, '2025-05-27'), due_date)) * 1.0
                      WHEN fee_strategy = 'quantity' THEN
                          CASE WHEN DATEDIFF(COALESCE(return_date, '2025-05-27'), due_date) > 0 THEN 5.0 ELSE 0.0 END
                      ELSE
                          0.0
    END
WHERE return_date IS NOT NULL OR due_date < '2025-05-27';

-- Tắt an toàn dữ liệu =0 , bật lại =1
SET SQL_SAFE_UPDATES = 0;
SET SQL_SAFE_UPDATES = 1;


-- Kiểm tra kết quả
SELECT id, book_id, member_id, due_date, return_date, overdue_fee, fee_strategy
FROM loans;

-- Truy vấn
SELECT * FROM loans WHERE return_date IS NULL;

-- Xóa danh sách đã mượn ở quản lý mượn sách admin và đặt id lại về 1
DELETE FROM loans;
ALTER TABLE loans AUTO_INCREMENT = 1;

ALTER TABLE members MODIFY username VARCHAR(255) NULL;
ALTER TABLE members DROP INDEX username; -- bỏ ràng buộc UNIQUE

