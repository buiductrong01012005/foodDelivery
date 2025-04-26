
CREATE DATABASE IF NOT EXISTS `simple_food_delivery_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `simple_food_delivery_db`;


SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `order_status_history`; 
DROP TABLE IF EXISTS `driver_reviews`;
DROP TABLE IF EXISTS `restaurant_reviews`;
DROP TABLE IF EXISTS `food_reviews`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `drivers`;
DROP TABLE IF EXISTS `addresses`;
DROP TABLE IF EXISTS `cart_items`;
DROP TABLE IF EXISTS `carts`;
DROP TABLE IF EXISTS `foods`;
DROP TABLE IF EXISTS `food_categories`;
DROP TABLE IF EXISTS `restaurants`;
DROP TABLE IF EXISTS `password_resets`; 
DROP TABLE IF EXISTS `users`;


SET FOREIGN_KEY_CHECKS=1;

-- Bảng: users

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    date_of_birth DATE NULL,
    phone_number VARCHAR(20) NULL UNIQUE,
    gender ENUM('Male', 'Female', 'Other') NULL,
    profile_picture_url VARCHAR(500) NULL,
    role ENUM('Customer', 'Admin') NOT NULL DEFAULT 'Customer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- Bảng: food_categories

CREATE TABLE food_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL,
    image_url VARCHAR(500) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- Bảng: foods

CREATE TABLE foods (
    food_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    availability_status ENUM('Available', 'Unavailable') NOT NULL DEFAULT 'Available',
    image_url VARCHAR(500) NULL,
    created_by INT NULL,
    updated_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES food_categories(category_id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL
);


-- Bảng: carts

CREATE TABLE carts (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Bảng: cart_items

CREATE TABLE cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(food_id) ON DELETE CASCADE,
    UNIQUE KEY `cart_food_item` (`cart_id`, `food_id`)
);

-- Bảng: addresses

CREATE TABLE addresses (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100) NULL,
    ward VARCHAR(100) NULL,
    address_label VARCHAR(50) NULL,
    is_default BOOLEAN DEFAULT FALSE,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Bảng: orders

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    delivery_address_id INT NOT NULL,
    order_code VARCHAR(20) UNIQUE NOT NULL,
    food_cost DECIMAL(12, 2) NOT NULL,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL,
    delivery_method ENUM('Standard', 'Express') NOT NULL DEFAULT 'Standard',
    order_status ENUM('Pending', 'Processing', 'Shipped', 'Delivered', 'CancelledByUser', 'CancelledByAdmin', 'FailedDelivery') NOT NULL DEFAULT 'Pending',
    payment_method ENUM('COD', 'OnlineBanking', 'Card', 'EWallet') NULL,
    payment_status ENUM('Pending', 'Paid', 'Failed', 'Refunded') NOT NULL DEFAULT 'Pending',
    special_instructions TEXT NULL,
    placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shipped_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    estimated_delivery_time TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (delivery_address_id) REFERENCES addresses(address_id) ON DELETE RESTRICT
);

-- Bảng: order_items

CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_at_order DECIMAL(10, 2) NOT NULL,
    item_subtotal DECIMAL(12, 2) NOT NULL,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(food_id) ON DELETE RESTRICT
);

-- Bảng: food_reviews

CREATE TABLE food_reviews (
    food_review_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    food_id INT NOT NULL,
    rating SMALLINT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(food_id) ON DELETE CASCADE,
    UNIQUE KEY `user_order_food_review` (`user_id`, `order_id`, `food_id`)
);

-- Bảng: order_status_history

CREATE TABLE order_status_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    status VARCHAR(50) NOT NULL, 
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT NULL, -- Ghi chú thêm

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- Bảng: password_resets

CREATE TABLE password_resets (
    email VARCHAR(255) NOT NULL PRIMARY KEY, -- Email của người yêu cầu reset
    token VARCHAR(255) NOT NULL UNIQUE, -- Token ngẫu nhiên, duy nhất gửi qua email
    expires_at TIMESTAMP NOT NULL, -- Thời gian token hết hạn

    FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE
);



-- Indexes

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_food_categories_name ON food_categories(name);
CREATE INDEX idx_foods_category_id ON foods(category_id);
CREATE INDEX idx_foods_name ON foods(name);
CREATE INDEX idx_foods_price ON foods(price);
CREATE INDEX idx_foods_availability ON foods(availability_status);
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_food_id ON cart_items(food_id);
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_placed_at ON orders(placed_at);
CREATE INDEX idx_orders_order_code ON orders(order_code);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_food_id ON order_items(food_id);
CREATE INDEX idx_food_reviews_order_id ON food_reviews(order_id);
CREATE INDEX idx_food_reviews_user_id ON food_reviews(user_id);
CREATE INDEX idx_food_reviews_food_id ON food_reviews(food_id);

CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);
CREATE INDEX idx_order_status_history_changed_at ON order_status_history(changed_at);

CREATE INDEX idx_password_resets_token ON password_resets(token);
CREATE INDEX idx_password_resets_expires_at ON password_resets(expires_at);



-- Bảng: users (Khách hàng & Admin)

INSERT INTO `users` (`user_id`, `full_name`, `email`, `password_hash`, `date_of_birth`, `phone_number`, `gender`, `profile_picture_url`, `role`) VALUES
(1, 'Nguyễn Lan Anh', 'lananh.nguyen@email.com', 'lananh1998', '1998-05-20', '0912345678', 'Female', 'https://i.pravatar.cc/150?img=1', 'Customer'),
(2, 'Trần Minh Đức', 'duc.tran@email.com', 'duc1995', '1995-11-02', '0987654321', 'Male', 'https://i.pravatar.cc/150?img=2', 'Customer'),
(3, 'Lê Thu Hà', 'ha.le@email.com', 'ha2001', '2001-09-15', '0905112233', 'Female', NULL, 'Customer'),
(4, 'Phạm Tiến Dũng', 'dung.pham@email.com', 'dung1993', '1993-03-10', '0333444555', 'Male', 'https://i.pravatar.cc/150?img=4', 'Customer'),
(5, 'Vũ Thị Mai', 'mai.vu@email.com', 'mai2000', '2000-07-07', '0777888999', 'Female', NULL, 'Customer'),
(6, 'Hoàng Công Vinh', 'vinh.hoang@email.com', 'vinh1996', '1996-12-25', '0944555666', 'Male', 'https://i.pravatar.cc/150?img=6', 'Customer'),
(7, 'Đặng Bảo Ngọc', 'ngoc.dang@email.com', 'ngoc2002', '2002-01-30', '0888123456', 'Female', 'https://i.pravatar.cc/150?img=7', 'Customer'),
(8, 'Bùi Chí Thanh', 'thanh.bui@email.com', 'thanh1990', '1990-04-05', '0765987654', 'Male', NULL, 'Customer'),
(9, 'Hồ Quỳnh Hương', 'huong.ho@email.com', 'huong1999', '1999-08-18', '0939123123', 'Female', 'https://i.pravatar.cc/150?img=9', 'Customer'),
(10, 'Ngô Minh Khang', 'khang.ngo@email.com', 'khang1997', '1997-06-12', '0928456456', 'Male', NULL, 'Customer'),
(11, 'Quản Trị Viên A', 'admin@foodapp.vn', 'admin1985', '1985-01-01', '0909090901', 'Other', NULL, 'Admin'),
(12, 'Super Admin', 'superadmin@foodapp.vn', 'superadmin1980', '1980-02-02', '0909090902', 'Other', NULL, 'Admin');


-- Bảng: food_categories

INSERT INTO `food_categories` (`category_id`, `name`, `description`, `image_url`, `is_active`) VALUES
(1, 'Cơm Phần', 'Cơm trắng ăn kèm các món mặn, xào, canh', 'https://example.com/cat/comphan.jpg', 1),
(2, 'Bún/Phở/Mì', 'Các món nước hoặc trộn phổ biến', 'https://example.com/cat/bunphomi.jpg', 1),
(3, 'Bánh Mì', 'Bánh mì kẹp thịt, pate, chả các loại', 'https://example.com/cat/banhmi.jpg', 1),
(4, 'Món Cuốn/Gỏi', 'Các món cuốn và gỏi thanh mát', 'https://example.com/cat/goicuon.jpg', 1),
(5, 'Đồ Ăn Vặt', 'Xiên que, bánh tráng, đồ chiên...', 'https://example.com/cat/anvat.jpg', 1),
(6, 'Món Chay', 'Các món ăn dành cho người ăn chay', 'https://example.com/cat/monchay.jpg', 1),
(7, 'Tráng Miệng', 'Chè, bánh ngọt, trái cây', 'https://example.com/cat/trangmieng.jpg', 1),
(8, 'Đồ Uống', 'Trà sữa, cà phê, nước ép, sinh tố', 'https://example.com/cat/douong.jpg', 1),
(9, 'Món Nhậu', 'Các món phù hợp lai rai', NULL, 1),
(10, 'Healthy Food', 'Đồ ăn lành mạnh, salad, eat clean', 'https://example.com/cat/healthy.jpg', 1);


-- Bảng: foods (Do Admin quản lý)

INSERT INTO `foods` (`food_id`, `category_id`, `name`, `description`, `price`, `availability_status`, `image_url`, `created_by`, `updated_by`) VALUES
(1, 1, 'Cơm Sườn Bì Chả', 'Cơm tấm nóng hổi với sườn cốt lết nướng, bì heo và chả trứng', 45000.00, 'Available', 'https://example.com/food/comsuonbicha.jpg', 11, 11),
(2, 1, 'Cơm Gà Rôti', 'Đùi gà được rôti vàng óng, da giòn, thịt mềm, ăn kèm cơm trắng', 50000.00, 'Available', 'https://example.com/food/comgaroti.jpg', 11, 12),
(3, 2, 'Bún Chả Hà Nội', 'Bún tươi, chả nướng than hoa thơm lừng, nem rán (tùy chọn), rau sống và nước chấm chua ngọt', 55000.00, 'Available', 'https://example.com/food/buncha.jpg', 12, 12),
(4, 2, 'Phở Bò Đặc Biệt', 'Tô phở đầy đặn với tái, nạm, gân, gầu, nước dùng đậm đà', 65000.00, 'Available', 'https://example.com/food/phobodb.jpg', 11, 11),
(5, 3, 'Bánh Mì Thịt Nướng', 'Bánh mì giòn kẹp thịt nướng tẩm ướp đậm đà, đồ chua, rau thơm', 25000.00, 'Available', 'https://example.com/food/banhmithitnuong.jpg', 12, 12),
(6, 4, 'Gỏi Cuốn Tôm Thịt', 'Bánh tráng cuốn bún, tôm luộc, thịt ba chỉ luộc, rau sống, chấm mắm nêm hoặc tương đen', 10000.00, 'Available', 'https://example.com/food/goicuon.jpg', 11, 11),
(7, 5, 'Bánh Tráng Trộn Đặc Biệt', 'Bánh tráng cắt sợi trộn xoài xanh, khô bò, trứng cút, rau răm, đậu phộng, nước sốt me', 25000.00, 'Unavailable', 'https://example.com/food/banhtrangtron.jpg', 11, 12),
(8, 8, 'Trà Sữa Trân Châu Đường Đen', 'Trà sữa béo ngậy kết hợp trân châu đường đen dai ngon', 40000.00, 'Available', 'https://example.com/food/trasuadd.jpg', 12, 12),
(9, 7, 'Chè Khúc Bạch', 'Thạch khúc bạch mềm mịn, phô mai, nhãn lồng, hạnh nhân lát trong nước đường phèn thanh mát', 30000.00, 'Available', 'https://example.com/food/chekhucbach.jpg', 11, 11),
(10, 6, 'Cơm Chay Thập Cẩm', 'Cơm trắng ăn kèm nhiều loại rau củ xào, đậu hũ chiên, nấm kho', 35000.00, 'Available', 'https://example.com/food/comchay.jpg', 11, 11),
(11, 2, 'Mì Ý Sốt Bò Bằm', 'Mì Ý với sốt cà chua và thịt bò bằm đậm đà', 60000.00, 'Available', 'https://example.com/food/miy.jpg', 12, 12),
(12, 8, 'Nước Ép Cam Tươi', '100% cam tươi vắt nguyên chất', 30000.00, 'Available', 'https://example.com/food/camep.jpg', 11, 11);


-- Bảng: addresses (Địa chỉ của Khách hàng)

INSERT INTO `addresses` (`address_id`, `user_id`, `street_address`, `city`, `district`, `ward`, `address_label`, `is_default`, `latitude`, `longitude`) VALUES
(1, 1, '123 Đường ABC, Phường 1', 'TP. Hồ Chí Minh', 'Quận 1', 'P. Bến Nghé', 'Nhà riêng', 1, 10.7756, 106.7019),
(2, 1, 'Tòa nhà Bitexco, 2 Hải Triều', 'TP. Hồ Chí Minh', 'Quận 1', 'P. Bến Nghé', 'Công ty', 0, 10.7719, 106.7044),
(3, 2, 'Số 10, Ngõ 100, Phố XYZ', 'Hà Nội', 'Ba Đình', 'P. Giảng Võ', 'Nhà riêng', 1, 21.0278, 105.8342),
(4, 3, 'Chung cư Vinhomes Ocean Park', 'Hà Nội', 'Gia Lâm', 'Xã Đa Tốn', 'Nhà', 1, 20.9980, 105.9470),
(5, 4, 'Khu công nghệ cao Hòa Lạc', 'Hà Nội', 'Thạch Thất', NULL, 'Công ty', 1, 21.0200, 105.5200),
(6, 5, 'Đại học Đà Nẵng, 41 Lê Duẩn', 'Đà Nẵng', 'Hải Châu', 'P. Hải Châu 1', 'Trường học', 1, 16.0690, 108.2221),
(7, 6, 'Vincom Center Đà Nẵng, Ngô Quyền', 'Đà Nẵng', 'Sơn Trà', 'P. An Hải Bắc', 'Trung tâm TM', 1, 16.0748, 108.2315),
(8, 7, 'Làng Đại học Quốc gia TP.HCM', 'Bình Dương', 'Dĩ An', 'P. Đông Hòa', 'KTX', 1, 10.8791, 106.8001),
(9, 8, 'Khu đô thị Phú Mỹ Hưng', 'TP. Hồ Chí Minh', 'Quận 7', 'P. Tân Phong', 'Nhà riêng', 1, 10.7293, 106.7025),
(10, 9, 'Cầu Rồng', 'Đà Nẵng', 'Sơn Trà', 'P. An Hải Tây', 'Điểm hẹn', 0, 16.0614, 108.2274);


-- Bảng: carts (Ví dụ 1 giỏ hàng)

INSERT INTO `carts` (`cart_id`, `user_id`) VALUES
(1, 1); -- Giỏ hàng của user Lan Anh


-- Bảng: cart_items (Các món trong giỏ hàng của Lan Anh)

INSERT INTO `cart_items` (`cart_item_id`, `cart_id`, `food_id`, `quantity`) VALUES
(1, 1, 3, 1), -- 1 Bún Chả
(2, 1, 8, 1); -- 1 Trà Sữa


-- Bảng: orders (Lịch sử đặt hàng)

-- Order 1 (Đã giao)
INSERT INTO `orders` (`order_id`, `user_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `delivery_method`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `shipped_at`, `delivered_at`, `estimated_delivery_time`) VALUES
(1, 2, 3, 'FDHN24A001', 115000.00, 15000.00, 0.00, 130000.00, 'Standard', 'Delivered', 'COD', 'Paid', 'Gọi điện trước khi giao.', '2024-05-20 11:00:00', '2024-05-20 11:35:00', '2024-05-20 12:10:00', '2024-05-20 12:15:00');
-- Order 2 (Đang giao)
INSERT INTO `orders` (`order_id`, `user_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `delivery_method`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `shipped_at`, `estimated_delivery_time`) VALUES
(2, 3, 4, 'FDHN24B002', 25000.00, 25000.00, 5000.00, 45000.00, 'Express', 'Shipped', 'EWallet', 'Paid', 'Giao nhanh giúp mình!', '2024-05-21 18:00:00', '2024-05-21 18:20:00', '2024-05-21 18:50:00');
-- Order 3 (Đang xử lý)
INSERT INTO `orders` (`order_id`, `user_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `delivery_method`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `estimated_delivery_time`) VALUES
(3, 5, 6, 'FDDN24C003', 70000.00, 10000.00, 0.00, 80000.00, 'Standard', 'Processing', 'Card', 'Paid', 'Lấy thêm ớt.', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR));
-- Order 4 (Chờ xử lý)
INSERT INTO `orders` (`order_id`, `user_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `delivery_method`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `estimated_delivery_time`) VALUES
(4, 9, 10, 'FDDN24D004', 60000.00, 15000.00, 10000.00, 65000.00, 'Standard', 'Pending', 'COD', 'Pending', NULL, DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_ADD(NOW(), INTERVAL 50 MINUTE));
-- Order 5 (Đã hủy bởi Khách)
INSERT INTO `orders` (`order_id`, `user_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `delivery_method`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `updated_at`) VALUES
(5, 7, 8, 'FDHCM24E005', 40000.00, 12000.00, 0.00, 52000.00, 'Standard', 'CancelledByUser', 'EWallet', 'Refunded', NULL, '2024-05-19 09:00:00', '2024-05-19 09:15:00');
-- Order 6 (Đã giao)
INSERT INTO `orders` (`order_id`, `user_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `delivery_method`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `shipped_at`, `delivered_at`, `estimated_delivery_time`) VALUES
(6, 1, 1, 'FDHCM24F006', 60000.00, 16000.00, 0.00, 76000.00, 'Standard', 'Delivered', 'Card', 'Paid', NULL, '2024-05-18 12:30:00', '2024-05-18 12:55:00', '2024-05-18 13:30:00', '2024-05-18 13:40:00');


-- Bảng: order_items (Chi tiết cho các đơn hàng)

-- Order 1 items
INSERT INTO `order_items` (`order_item_id`, `order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(1, 1, 4, 1, 65000.00, 65000.00), -- 1 Phở bò đặc biệt
(2, 1, 12, 1, 30000.00, 30000.00), -- 1 Cam ép
(3, 1, 9, 1, 30000.00, 30000.00); -- 1 Chè khúc bạch
-- Order 2 items
INSERT INTO `order_items` (`order_item_id`, `order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(4, 2, 5, 1, 25000.00, 25000.00); -- 1 Bánh mì thịt nướng
-- Order 3 items
INSERT INTO `order_items` (`order_item_id`, `order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(5, 3, 10, 2, 35000.00, 70000.00); -- 2 Cơm chay thập cẩm
-- Order 4 items
INSERT INTO `order_items` (`order_item_id`, `order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(6, 4, 11, 1, 60000.00, 60000.00); -- 1 Mì Ý
-- Order 5 items
INSERT INTO `order_items` (`order_item_id`, `order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(7, 5, 8, 1, 40000.00, 40000.00); -- 1 Trà sữa
-- Order 6 items
INSERT INTO `order_items` (`order_item_id`, `order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(8, 6, 11, 1, 60000.00, 60000.00); -- 1 Mì Ý


-- Bảng: food_reviews (Cho các đơn đã giao - Order 1, Order 6)

INSERT INTO `food_reviews` (`food_review_id`, `order_id`, `user_id`, `food_id`, `rating`, `comment`) VALUES
(1, 1, 2, 4, 5, 'Phở rất ngon, nước dùng đậm đà!'), -- User Đức review Phở
(2, 1, 2, 12, 4, 'Cam ép hơi ngọt so với mình.'), -- User Đức review Cam ép
(3, 6, 1, 11, 5, 'Mì Ý sốt ngon, vừa miệng.'); -- User Lan Anh review Mì Ý


-- Bảng: order_status_history 

-- Order 1 History
INSERT INTO `order_status_history` (`history_id`, `order_id`, `status`, `changed_at`, `notes`) VALUES
(1, 1, 'Pending', '2024-05-20 11:00:00', NULL),
(2, 1, 'Processing', '2024-05-20 11:05:00', 'Admin 11 xử lý'),
(3, 1, 'Shipped', '2024-05-20 11:35:00', NULL),
(4, 1, 'Delivered', '2024-05-20 12:10:00', 'Đã giao thành công');
-- Order 2 History
INSERT INTO `order_status_history` (`history_id`, `order_id`, `status`, `changed_at`, `notes`) VALUES
(5, 2, 'Pending', '2024-05-21 18:00:00', NULL),
(6, 2, 'Processing', '2024-05-21 18:05:00', 'Admin 12 xử lý'),
(7, 2, 'Shipped', '2024-05-21 18:20:00', 'Giao hàng nhanh');
-- Order 3 History
INSERT INTO `order_status_history` (`history_id`, `order_id`, `status`, `changed_at`, `notes`) VALUES
(8, 3, 'Pending', NOW(), NULL),
(9, 3, 'Processing', DATE_ADD(NOW(), INTERVAL 2 MINUTE), 'Admin 11 đang xử lý'); -- Giả sử đang xử lý
-- Order 4 History
INSERT INTO `order_status_history` (`history_id`, `order_id`, `status`, `changed_at`, `notes`) VALUES
(10, 4, 'Pending', DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL); -- Vẫn đang pending
-- Order 5 History
INSERT INTO `order_status_history` (`history_id`, `order_id`, `status`, `changed_at`, `notes`) VALUES
(11, 5, 'Pending', '2024-05-19 09:00:00', NULL),
(12, 5, 'CancelledByUser', '2024-05-19 09:15:00', 'Người dùng yêu cầu hủy');
-- Order 6 History
INSERT INTO `order_status_history` (`history_id`, `order_id`, `status`, `changed_at`, `notes`) VALUES
(13, 6, 'Pending', '2024-05-18 12:30:00', NULL),
(14, 6, 'Processing', '2024-05-18 12:35:00', 'Admin 11 xử lý'),
(15, 6, 'Shipped', '2024-05-18 12:55:00', NULL),
(16, 6, 'Delivered', '2024-05-18 13:30:00', 'Đã giao thành công');


-- Bảng: password_resets (Ví dụ 1 yêu cầu reset)

INSERT INTO `password_resets` (`email`, `token`, `expires_at`) VALUES
('dung.pham@email.com', 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8', DATE_ADD(NOW(), INTERVAL 1 HOUR)); -- Token cho user Dũng, hết hạn sau 1 giờ