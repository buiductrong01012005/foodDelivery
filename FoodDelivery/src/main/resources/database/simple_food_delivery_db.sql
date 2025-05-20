
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
    status ENUM('Show', 'Hide') DEFAULT 'Show' NOT NULL,
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

SET FOREIGN_KEY_CHECKS=0;

-- Bảng: users (Tổng cộng 20 bản ghi)
INSERT INTO `users` (`user_id`, `full_name`, `email`, `password_hash`, `date_of_birth`, `phone_number`, `gender`, `profile_picture_url`, `role`, `created_at`, `updated_at`) VALUES
(1, 'Nguyễn Lan Anh', 'lananh.nguyen@email.com', 'lananh1998', '1998-05-20', '0912345678', 'Female', 'https://i.pravatar.cc/150?img=1', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(2, 'Trần Minh Đức', 'duc.tran@email.com', 'duc1995', '1995-11-02', '0987654321', 'Male', 'https://i.pravatar.cc/150?img=2', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(3, 'Lê Thu Hà', 'ha.le@email.com', 'ha2001', '2001-09-15', '0905112233', 'Female', NULL, 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(4, 'Phạm Tiến Dũng', 'dung.pham@email.com', 'dung1993', '1993-03-10', '0333444555', 'Male', 'https://i.pravatar.cc/150?img=4', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(5, 'Vũ Thị Mai', 'mai.vu@email.com', 'mai2000', '2000-07-07', '0777888999', 'Female', NULL, 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(6, 'Hoàng Công Vinh', 'vinh.hoang@email.com', 'vinh1996', '1996-12-25', '0944555666', 'Male', 'https://i.pravatar.cc/150?img=6', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(7, 'Đặng Bảo Ngọc', 'ngoc.dang@email.com', 'ngoc2002', '2002-01-30', '0888123456', 'Female', 'https://i.pravatar.cc/150?img=7', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(8, 'Bùi Chí Thanh', 'thanh.bui@email.com', 'thanh1990', '1990-04-05', '0765987654', 'Male', NULL, 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(9, 'Hồ Quỳnh Hương', 'huong.ho@email.com', 'huong1999', '1999-08-18', '0939123123', 'Female', 'https://i.pravatar.cc/150?img=9', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(10, 'Ngô Minh Khang', 'khang.ngo@email.com', 'khang1997', '1997-06-12', '0928456456', 'Male', NULL, 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(11, 'Quản Trị Viên A', 'admin@foodapp.vn', 'admin1985', '1985-01-01', '0909090901', 'Other', NULL, 'Admin', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(12, 'Super Admin', 'superadmin@foodapp.vn', 'superadmin1980', '1980-02-02', '0909090902', 'Other', NULL, 'Admin', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(13, 'Trần Văn An', 'an.tran@email.com', 'an1992', '1992-02-14', '0911223344', 'Male', 'https://i.pravatar.cc/150?img=13', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(14, 'Lê Thị Bình', 'binh.le@email.com', 'binh1988', '1988-07-21', '0922334455', 'Female', NULL, 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(15, 'Phạm Quốc Cường', 'cuong.pham@email.com', 'cuong1995', '1995-09-03', '0933445566', 'Male', 'https://i.pravatar.cc/150?img=15', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(16, 'Võ Mỹ Duyên', 'duyen.vo@email.com', 'duyen2000', '2000-11-11', '0944556677', 'Female', NULL, 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(17, 'Đỗ Gia Hưng', 'hung.do@email.com', 'hung1985', '1985-04-30', '0955667788', 'Male', 'https://i.pravatar.cc/150?img=17', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(18, 'Nguyễn Khánh Linh', 'linh.nguyen.khanh@email.com', 'linh1997', '1997-01-01', '0966778899', 'Female', 'https://i.pravatar.cc/150?img=18', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(19, 'Hoàng Minh Quân', 'quan.hoang@email.com', 'quan1993', '1993-06-15', '0977889900', 'Male', NULL, 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(20, 'Triệu Thanh Thảo', 'thao.trieu@email.com', 'thao1999', '1999-08-20', '0988990011', 'Female', 'https://i.pravatar.cc/150?img=20', 'Customer', '2024-05-22 00:00:00', '2024-05-22 00:00:00');

-- Bảng: food_categories (Tổng cộng 20 bản ghi)
INSERT INTO `food_categories` (`category_id`, `name`, `description`, `image_url`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'Cơm Phần', 'Cơm trắng ăn kèm các món mặn, xào, canh', 'https://example.com/cat/comphan.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(2, 'Bún/Phở/Mì', 'Các món nước hoặc trộn phổ biến', 'https://example.com/cat/bunphomi.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(3, 'Bánh Mì', 'Bánh mì kẹp thịt, pate, chả các loại', 'https://example.com/cat/banhmi.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(4, 'Món Cuốn/Gỏi', 'Các món cuốn và gỏi thanh mát', 'https://example.com/cat/goicuon.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(5, 'Đồ Ăn Vặt', 'Xiên que, bánh tráng, đồ chiên...', 'https://example.com/cat/anvat.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(6, 'Món Chay', 'Các món ăn dành cho người ăn chay', 'https://example.com/cat/monchay.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(7, 'Tráng Miệng', 'Chè, bánh ngọt, trái cây', 'https://example.com/cat/trangmieng.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(8, 'Đồ Uống', 'Trà sữa, cà phê, nước ép, sinh tố', 'https://example.com/cat/douong.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(9, 'Món Nhậu', 'Các món phù hợp lai rai', NULL, 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(10, 'Healthy Food', 'Đồ ăn lành mạnh, salad, eat clean', 'https://example.com/cat/healthy.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(11, 'Pizza', 'Các loại pizza đế dày, đế mỏng', 'https://example.com/cat/pizza.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(12, 'Hamburger & Sandwich', 'Hamburger bò, gà và các loại sandwich', 'https://example.com/cat/burger.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(13, 'Gà Rán & Khoai Tây Chiên', 'Gà rán giòn, khoai tây chiên các vị', 'https://example.com/cat/friedchicken.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(14, 'Lẩu', 'Các loại lẩu thái, lẩu nấm, lẩu hải sản', 'https://example.com/cat/hotpot.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(15, 'Đồ Nướng BBQ', 'Thịt nướng, hải sản nướng xiên que hoặc tảng', 'https://example.com/cat/bbq.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(16, 'Sushi & Sashimi', 'Các món ăn Nhật Bản tươi sống', 'https://example.com/cat/sushi.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(17, 'Cơm Văn Phòng Khác', 'Các set cơm trưa đa dạng cho dân văn phòng', 'https://example.com/cat/comvanphong2.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(18, 'Cháo & Súp', 'Cháo dinh dưỡng, súp cua, súp gà', 'https://example.com/cat/chao_sup.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(19, 'Bánh Ngọt & Bánh Kem', 'Các loại bánh ngọt, bánh kem sinh nhật', 'https://example.com/cat/bakery.jpg', 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(20, 'Đặc Sản Vùng Miền', 'Các món ăn đặc trưng của các địa phương', NULL, 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00');

-- Bảng: foods (Tổng cộng 20 bản ghi)
INSERT INTO `foods` (`food_id`, `category_id`, `name`, `description`, `price`, `availability_status`, `image_url`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
(1, 1, 'Cơm Sườn Bì Chả', 'Cơm tấm nóng hổi với sườn cốt lết nướng, bì heo và chả trứng', 45000.00, 'Available', 'images/comsuonbicha.jpg', 11, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(2, 1, 'Cơm Gà Rôti', 'Đùi gà được rôti vàng óng, da giòn, thịt mềm, ăn kèm cơm trắng', 50000.00, 'Available', 'images/comgaroti.jpg', 11, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(3, 2, 'Bún Chả Hà Nội', 'Bún tươi, chả nướng than hoa thơm lừng, nem rán (tùy chọn), rau sống và nước chấm chua ngọt', 55000.00, 'Available', 'images/buncha.jpg', 12, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(4, 2, 'Phở Bò Đặc Biệt', 'Tô phở đầy đặn với tái, nạm, gân, gầu, nước dùng đậm đà', 65000.00, 'Available', 'images/phobodb.jpg', 11, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(5, 3, 'Bánh Mì Thịt Nướng', 'Bánh mì giòn kẹp thịt nướng tẩm ướp đậm đà, đồ chua, rau thơm', 25000.00, 'Available', 'images/banhmithitnuong.jpg', 12, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(6, 4, 'Gỏi Cuốn Tôm Thịt', 'Bánh tráng cuốn bún, tôm luộc, thịt ba chỉ luộc, rau sống, chấm mắm nêm hoặc tương đen', 10000.00, 'Available', 'images/goicuon.jpg', 11, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(7, 5, 'Bánh Tráng Trộn Đặc Biệt', 'Bánh tráng cắt sợi trộn xoài xanh, khô bò, trứng cút, rau răm, đậu phộng, nước sốt me', 25000.00, 'Unavailable', 'images/banhtrangtron.jpg', 11, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(8, 8, 'Trà Sữa Trân Châu Đường Đen', 'Trà sữa béo ngậy kết hợp trân châu đường đen dai ngon', 40000.00, 'Available', 'images/trasuadd.jpg', 12, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(9, 7, 'Chè Khúc Bạch', 'Thạch khúc bạch mềm mịn, phô mai, nhãn lồng, hạnh nhân lát trong nước đường phèn thanh mát', 30000.00, 'Available', 'images/chekhucbach.jpg', 11, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(10, 6, 'Cơm Chay Thập Cẩm', 'Cơm trắng ăn kèm nhiều loại rau củ xào, đậu hũ chiên, nấm kho', 35000.00, 'Available', 'images/comchay.jpg', 11, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(11, 2, 'Mì Ý Sốt Bò Bằm', 'Mì Ý với sốt cà chua và thịt bò bằm đậm đà', 60000.00, 'Available', 'images/miy.jpg', 12, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(12, 8, 'Nước Ép Cam Tươi', '100% cam tươi vắt nguyên chất', 30000.00, 'Available', 'images/camep.jpg', 11, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(13, 11, 'Pizza Hải Sản Cao Cấp', 'Pizza với tôm, mực, cua, phô mai mozzarella', 250000.00, 'Available', 'images/pizzahaisan.jpg', 11, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(14, 13, 'Combo Gà Rán Gia Đình', '5 miếng gà rán, 2 khoai tây chiên lớn, 2 nước ngọt', 199000.00, 'Available', 'images/comboran.jpg', 12, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(15, 12, 'Hamburger Bò Phô Mai Đặc Biệt', 'Bò Úc xay, phô mai cheddar, xà lách, cà chua', 89000.00, 'Available', 'images/burgerbo.jpg', 11, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(16, 16, 'Set Sushi Tổng Hợp (Nhỏ)', 'Cá hồi, cá ngừ, tôm, trứng cuộn (8 miếng)', 150000.00, 'Available', 'images/sushiset.jpg', 12, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(17, 18, 'Cháo Sườn Sụn Non', 'Cháo nấu nhừ với sườn sụn non, thêm quẩy giòn', 35000.00, 'Available', 'images/chaoson.jpg', 11, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(18, 14, 'Lẩu Thái Tomyum Hải Sản (2 người)', 'Nước lẩu chua cay, tôm, mực, nghêu, rau nấm', 280000.00, 'Available', 'images/lauthai.jpg', 12, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(19, 17, 'Cơm Gà Xối Mỡ Kèm Canh Rong Biển', 'Đùi gà xối mỡ da giòn, cơm trắng, canh rong biển', 55000.00, 'Available', 'images/comgaxoimo.jpg', 11, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(20, 15, 'Ba Chỉ Bò Mỹ Nướng Sốt BBQ', '200g ba chỉ bò Mỹ thái lát, ướp sốt BBQ', 120000.00, 'Available', 'images/bachibonuong.jpg', 12, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00');

-- Bảng: addresses (Tổng cộng 20 bản ghi)
INSERT INTO `addresses` (`address_id`, `user_id`, `street_address`, `city`, `district`, `ward`, `address_label`, `is_default`, `latitude`, `longitude`, `created_at`, `updated_at`) VALUES
(1, 1, '123 Đường ABC, Phường 1', 'TP. Hồ Chí Minh', 'Quận 1', 'P. Bến Nghé', 'Nhà riêng', 1, 10.7756, 106.7019, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(2, 1, 'Tòa nhà Bitexco, 2 Hải Triều', 'TP. Hồ Chí Minh', 'Quận 1', 'P. Bến Nghé', 'Công ty', 0, 10.7719, 106.7044, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(3, 2, 'Số 10, Ngõ 100, Phố XYZ', 'Hà Nội', 'Ba Đình', 'P. Giảng Võ', 'Nhà riêng', 1, 21.0278, 105.8342, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(4, 3, 'Chung cư Vinhomes Ocean Park', 'Hà Nội', 'Gia Lâm', 'Xã Đa Tốn', 'Nhà', 1, 20.9980, 105.9470, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(5, 4, 'Khu công nghệ cao Hòa Lạc', 'Hà Nội', 'Thạch Thất', NULL, 'Công ty', 1, 21.0200, 105.5200, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(6, 5, 'Đại học Đà Nẵng, 41 Lê Duẩn', 'Đà Nẵng', 'Hải Châu', 'P. Hải Châu 1', 'Trường học', 1, 16.0690, 108.2221, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(7, 6, 'Vincom Center Đà Nẵng, Ngô Quyền', 'Đà Nẵng', 'Sơn Trà', 'P. An Hải Bắc', 'Trung tâm TM', 1, 16.0748, 108.2315, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(8, 7, 'Làng Đại học Quốc gia TP.HCM', 'Bình Dương', 'Dĩ An', 'P. Đông Hòa', 'KTX', 1, 10.8791, 106.8001, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(9, 8, 'Khu đô thị Phú Mỹ Hưng', 'TP. Hồ Chí Minh', 'Quận 7', 'P. Tân Phong', 'Nhà riêng', 1, 10.7293, 106.7025, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(10, 9, 'Cầu Rồng', 'Đà Nẵng', 'Sơn Trà', 'P. An Hải Tây', 'Điểm hẹn', 0, 16.0614, 108.2274, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(11, 13, '250 Đường Láng', 'Hà Nội', 'Đống Đa', 'P. Láng Thượng', 'Nhà riêng', 1, 21.0109, 105.8112, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(12, 14, 'Tòa nhà Keangnam, Đường Phạm Hùng', 'Hà Nội', 'Nam Từ Liêm', 'P. Mễ Trì', 'Công ty', 1, 21.0169, 105.7830, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(13, 15, '55 Nguyễn Văn Cừ', 'TP. Hồ Chí Minh', 'Quận 5', 'P. Nguyễn Cư Trinh', 'Nhà riêng', 1, 10.7605, 106.6794, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(14, 16, 'Khu đô thị Sala, 10 Mai Chí Thọ', 'TP. Hồ Chí Minh', 'Quận 2', 'P. An Lợi Đông', 'Nhà', 1, 10.7771, 106.7294, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(15, 17, 'Cầu Sông Hàn', 'Đà Nẵng', 'Sơn Trà', 'P. An Hải Tây', 'Điểm hẹn', 1, 16.0678, 108.2276, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(16, 18, 'FPT Complex, KCN An Đồn', 'Đà Nẵng', 'Sơn Trà', 'P. An Hải Bắc', 'Công ty', 1, 16.0800, 108.2400, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(17, 19, 'Khu công nghiệp VSIP Bắc Ninh', 'Bắc Ninh', 'Từ Sơn', 'Xã Phù Chẩn', 'Nhà máy', 1, 21.1350, 105.9650, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(18, 20, 'Vincom Plaza Hạ Long', 'Quảng Ninh', 'Hạ Long', 'P. Bạch Đằng', 'Trung tâm TM', 1, 20.9518, 107.0737, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(19, 1, '300 Đường 3/2, Phường 10', 'TP. Hồ Chí Minh', 'Quận 10', 'P.10', 'Nhà bạn', 0, 10.7730, 106.6699, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(20, 2, 'Times City, 458 Minh Khai', 'Hà Nội', 'Hai Bà Trưng', 'P. Vĩnh Tuy', 'Chung cư', 0, 20.9973, 105.8660, '2024-05-22 00:00:00', '2024-05-22 00:00:00');

-- Bảng: carts (Tổng cộng 20 bản ghi)
INSERT INTO `carts` (`cart_id`, `user_id`, `created_at`, `updated_at`) VALUES
(1, 1, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(2, 2, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(3, 3, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(4, 4, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(5, 5, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(6, 6, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(7, 7, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(8, 8, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(9, 9, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(10, 10, '2024-05-22 00:00:00', '2024-05-22 00:00:00'),
(11, 13, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 13
(12, 14, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 14
(13, 15, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 15
(14, 16, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 16
(15, 17, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 17
(16, 18, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 18
(17, 19, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 19
(18, 20, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 20
(19, 11, '2024-05-22 00:00:00', '2024-05-22 00:00:00'), -- User 11 (Admin)
(20, 12, '2024-05-22 00:00:00', '2024-05-22 00:00:00'); -- User 12 (Admin)

-- Bảng: cart_items (Tổng cộng nhiều hơn 20 bản ghi)
INSERT INTO `cart_items` (`cart_item_id`, `cart_id`, `food_id`, `quantity`, `added_at`) VALUES
(1, 1, 3, 1, '2024-05-22 00:00:00'), (2, 1, 8, 1, '2024-05-22 00:00:00'),
(3, 2, 1, 1, '2024-05-22 00:00:00'), (4, 2, 10, 1, '2024-05-22 00:00:00'),
(5, 3, 13, 1, '2024-05-22 00:00:00'), (6, 3, 8, 2, '2024-05-22 00:00:00'),
(7, 4, 15, 1, '2024-05-22 00:00:00'),
(8, 5, 19, 1, '2024-05-22 00:00:00'), (9, 5, 12, 1, '2024-05-22 00:00:00'),
(10, 6, 14, 1, '2024-05-22 00:00:00'),
(11, 7, 17, 2, '2024-05-22 00:00:00'), (12, 7, 9, 1, '2024-05-22 00:00:00'),
(13, 8, 18, 1, '2024-05-22 00:00:00'), (14, 8, 4, 1, '2024-05-22 00:00:00'),
(15, 9, 20, 1, '2024-05-22 00:00:00'), (16, 9, 5, 1, '2024-05-22 00:00:00'),
(17, 10, 7, 1, '2024-05-22 00:00:00'), (18, 10, 16, 1, '2024-05-22 00:00:00'),
(19, 11, 2, 1, '2024-05-22 00:00:00'), (20, 11, 6, 3, '2024-05-22 00:00:00'),
(21, 12, 11, 1, '2024-05-22 00:00:00'), (22, 12, 1, 1, '2024-05-22 00:00:00'),
(23, 13, 13, 1, '2024-05-22 00:00:00'), (24, 13, 14, 1, '2024-05-22 00:00:00'),
(25, 14, 15, 1, '2024-05-22 00:00:00'), (26, 14, 8, 1, '2024-05-22 00:00:00'),
(27, 15, 17, 1, '2024-05-22 00:00:00'),
(28, 16, 19, 2, '2024-05-22 00:00:00'),
(29, 17, 20, 1, '2024-05-22 00:00:00'), (30, 17, 3, 1, '2024-05-22 00:00:00'),
(31, 18, 10, 1, '2024-05-22 00:00:00'), (32, 18, 12, 2, '2024-05-22 00:00:00'),
(33, 19, 4, 1, '2024-05-22 00:00:00'),
(34, 20, 16, 1, '2024-05-22 00:00:00');

-- Bảng: orders (Tổng cộng 21 bản ghi)
INSERT INTO `orders` (`order_id`, `user_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `delivery_method`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `shipped_at`, `delivered_at`, `estimated_delivery_time`, `updated_at`) VALUES
(1, 2, 3, 'FDHN24A001', 115000.00 + 10000.00 + 60000.00, 15000.00, 0.00, 130000.00 + 10000.00 + 60000.00, 'Standard', 'Delivered', 'COD', 'Paid', 'Gọi điện trước khi giao.', '2024-05-20 11:00:00', '2024-05-20 11:35:00', '2024-05-20 12:10:00', '2024-05-20 12:15:00', '2024-05-22 00:00:00'),
(2, 3, 4, 'FDHN24B002', 25000.00, 25000.00, 5000.00, 45000.00, 'Express', 'Shipped', 'EWallet', 'Paid', 'Giao nhanh giúp mình!', '2024-05-21 18:00:00', '2024-05-21 18:20:00', NULL, '2024-05-21 18:50:00', '2024-05-22 00:00:00'),
(3, 5, 6, 'FDDN24C003', 70000.00, 10000.00, 0.00, 80000.00, 'Standard', 'Processing', 'Card', 'Paid', 'Lấy thêm ớt.', '2024-05-22 10:00:00', NULL, NULL, '2024-05-22 11:00:00', '2024-05-22 10:02:00'),
(4, 9, 10, 'FDDN24D004', 60000.00, 15000.00, 10000.00, 65000.00, 'Standard', 'Pending', 'COD', 'Pending', NULL, '2024-05-22 09:50:00', NULL, NULL, '2024-05-22 10:40:00', '2024-05-22 09:50:00'),
(5, 7, 8, 'FDHCM24E005', 40000.00, 12000.00, 0.00, 52000.00, 'Standard', 'CancelledByUser', 'EWallet', 'Refunded', NULL, '2024-05-19 09:00:00', NULL, NULL, NULL, '2024-05-19 09:15:00'),
(6, 1, 1, 'FDHCM24F006', 60000.00 + 25000.00 + 40000.00 + 30000.00, 16000.00, 0.00, (60000.00 + 25000.00 + 40000.00 + 30000.00) + 16000.00, 'Standard', 'Delivered', 'Card', 'Paid', NULL, '2024-05-18 12:30:00', '2024-05-18 12:55:00', '2024-05-18 13:30:00', '2024-05-18 13:40:00', '2024-05-22 00:00:00'),
(7, 4, 5, 'REVORD001', 135000.00, 15000.00, 0.00, 150000.00, 'Standard', 'Delivered', 'COD', 'Paid', 'Giao hàng cẩn thận.', '2024-05-15 10:00:00', '2024-05-15 10:20:00', '2024-05-15 10:45:00', '2024-05-15 10:50:00', '2024-05-22 00:00:00'),
(8, 6, 7, 'REVORD002', 105000.00, 15000.00, 0.00, 120000.00, 'Standard', 'Delivered', 'OnlineBanking', 'Paid', NULL, '2024-05-16 11:00:00', '2024-05-16 11:25:00', '2024-05-16 11:50:00', '2024-05-16 11:55:00', '2024-05-22 00:00:00'),
(9, 8, 9, 'REVORD003', 120000.00, 18000.00, 0.00, 138000.00, 'Express', 'Delivered', 'EWallet', 'Paid', 'Không ớt.', '2024-05-17 12:00:00', '2024-05-17 12:15:00', '2024-05-17 12:40:00', '2024-05-17 12:45:00', '2024-05-22 00:00:00'),
(10, 3, 4, 'REVORD004', 170000.00, 20000.00, 0.00, 190000.00, 'Standard', 'Delivered', 'Card', 'Paid', 'Mang theo ống hút.', '2024-05-14 13:00:00', '2024-05-14 13:20:00', '2024-05-14 13:45:00', '2024-05-14 13:50:00', '2024-05-22 00:00:00'),
(11, 5, 6, 'REVORD005', 70000.00, 12000.00, 0.00, 82000.00, 'Standard', 'Delivered', 'COD', 'Paid', NULL, '2024-05-13 14:00:00', '2024-05-13 14:20:00', '2024-05-13 14:45:00', '2024-05-13 14:50:00', '2024-05-22 00:00:00'),
(12, 13, 11, 'FDNEW001', 85000.00, 15000.00, 0.00, 100000.00, 'Standard', 'Delivered', 'COD', 'Paid', 'Giao buổi trưa', '2024-05-10 10:00:00', '2024-05-10 10:30:00', '2024-05-10 11:00:00', '2024-05-10 11:10:00', '2024-05-22 00:00:00'),
(13, 14, 12, 'FDNEW002', 250000.00, 20000.00, 10000.00, 260000.00, 'Express', 'Delivered', 'OnlineBanking', 'Paid', NULL, '2024-05-11 11:30:00', '2024-05-11 11:45:00', '2024-05-11 12:15:00', '2024-05-11 12:20:00', '2024-05-22 00:00:00'),
(14, 15, 13, 'FDNEW003', 144000.00, 16000.00, 0.00, 160000.00, 'Standard', 'Delivered', 'Card', 'Paid', 'Không hành', '2024-05-12 17:00:00', '2024-05-12 17:20:00', '2024-05-12 17:50:00', '2024-05-12 18:00:00', '2024-05-22 00:00:00'),
(15, 16, 14, 'FDNEW004', 85000.00, 12000.00, 0.00, 97000.00, 'Standard', 'Delivered', 'EWallet', 'Paid', NULL, '2024-05-13 09:00:00', '2024-05-13 09:25:00', '2024-05-13 09:55:00', '2024-05-13 10:00:00', '2024-05-22 00:00:00'),
(16, 17, 15, 'FDNEW005', 280000.00, 25000.00, 20000.00, 285000.00, 'Express', 'Delivered', 'COD', 'Paid', 'Gọi trước 10 phút', '2024-05-14 19:00:00', '2024-05-14 19:15:00', '2024-05-14 19:40:00', '2024-05-14 19:45:00', '2024-05-22 00:00:00'),
(17, 18, 16, 'FDNEW006', 55000.00, 10000.00, 0.00, 65000.00, 'Standard', 'Delivered', 'OnlineBanking', 'Paid', NULL, '2024-05-15 12:30:00', '2024-05-15 12:50:00', '2024-05-15 13:20:00', '2024-05-15 13:25:00', '2024-05-22 00:00:00'),
(18, 19, 17, 'FDNEW007', 155000.00, 18000.00, 5000.00, 168000.00, 'Standard', 'Delivered', 'Card', 'Paid', 'Thêm tương ớt', '2024-05-16 15:00:00', '2024-05-16 15:30:00', '2024-05-16 16:00:00', '2024-05-16 16:10:00', '2024-05-22 00:00:00'),
(19, 20, 18, 'FDNEW008', 105000.00, 15000.00, 0.00, 120000.00, 'Express', 'Delivered', 'EWallet', 'Paid', NULL, '2024-05-17 08:00:00', '2024-05-17 08:10:00', '2024-05-17 08:30:00', '2024-05-17 08:35:00', '2024-05-22 00:00:00'),
(20, 1, 19, 'FDNEW009', 125000.00, 14000.00, 0.00, 139000.00, 'Standard', 'Processing', 'COD', 'Pending', 'Ít cay', '2024-05-22 09:30:00', NULL, NULL, '2024-05-22 10:30:00', '2024-05-22 09:35:00'),
(21, 4, 5, 'FDNEW010', 65000.00, 15000.00, 0.00, 80000.00, 'Standard', 'Pending', 'Card', 'Pending', NULL, '2024-05-22 10:00:00', NULL, NULL, '2024-05-22 10:45:00', '2024-05-22 10:00:00');


-- Bảng: order_items (Tổng cộng 47 bản ghi)
INSERT INTO `order_items` (`order_item_id`, `order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(1, 1, 4, 1, 65000.00, 65000.00), (2, 1, 12, 1, 30000.00, 30000.00), (3, 1, 9, 1, 30000.00, 30000.00),
(4, 2, 5, 1, 25000.00, 25000.00),
(5, 3, 10, 2, 35000.00, 70000.00),
(6, 4, 11, 1, 60000.00, 60000.00),
(7, 5, 8, 1, 40000.00, 40000.00),
(8, 6, 11, 1, 60000.00, 60000.00),
(9, 1, 6, 1, 10000.00, 10000.00),
(10, 1, 11, 1, 60000.00, 60000.00),
(11, 6, 5, 1, 25000.00, 25000.00),
(12, 6, 8, 1, 40000.00, 40000.00),
(13, 6, 12, 1, 30000.00, 30000.00),
(14, 7, 1, 1, 45000.00, 45000.00),  (15, 7, 2, 1, 50000.00, 50000.00),  (16, 7, 6, 1, 10000.00, 10000.00),  (17, 7, 9, 1, 30000.00, 30000.00),
(18, 8, 3, 1, 55000.00, 55000.00),  (19, 8, 5, 1, 25000.00, 25000.00),  (20, 8, 7, 1, 25000.00, 25000.00),
(21, 9, 1, 1, 45000.00, 45000.00),  (22, 9, 8, 1, 40000.00, 40000.00),  (23, 9, 10, 1, 35000.00, 35000.00),
(24, 10, 2, 1, 50000.00, 50000.00), (25, 10, 3, 1, 55000.00, 55000.00), (26, 10, 4, 1, 65000.00, 65000.00),
(27, 11, 6, 1, 10000.00, 10000.00), (28, 11, 7, 1, 25000.00, 25000.00), (29, 11, 10, 1, 35000.00, 35000.00),
(30, 12, 2, 1, 50000.00, 50000.00), (31, 12, 17, 1, 35000.00, 35000.00),
(32, 13, 13, 1, 250000.00, 250000.00),
(33, 14, 15, 1, 89000.00, 89000.00), (34, 14, 19, 1, 55000.00, 55000.00),
(35, 15, 10, 1, 35000.00, 35000.00), (36, 15, 8, 1, 40000.00, 40000.00), (37, 15, 6, 1, 10000.00, 10000.00),
(38, 16, 18, 1, 280000.00, 280000.00),
(39, 17, 19, 1, 55000.00, 55000.00),
(40, 18, 20, 1, 120000.00, 120000.00), (41, 18, 17, 1, 35000.00, 35000.00),
(42, 19, 1, 1, 45000.00, 45000.00), (43, 19, 2, 1, 50000.00, 50000.00), (44,19,6,1,10000.00,10000.00),
(45, 20, 4, 1, 65000.00, 65000.00), (46, 20, 11, 1, 60000.00, 60000.00),
(47, 21, 4, 1, 65000.00, 65000.00);

-- Bảng: food_reviews (Tổng cộng 42 bản ghi)
INSERT INTO `food_reviews` (`food_review_id`, `order_id`, `user_id`, `food_id`, `rating`, `comment`, `status`, `created_at`, `updated_at`) VALUES
(1, 1, 2, 4, 5, 'Phở rất ngon, nước dùng đậm đà!', 'Show', '2024-05-20 12:20:00', '2024-05-20 12:20:00'),
(2, 1, 2, 12, 4, 'Cam ép hơi ngọt so với mình.', 'Show', '2024-05-20 12:21:00', '2024-05-20 12:21:00'),
(3, 6, 1, 11, 5, 'Mì Ý sốt ngon, vừa miệng.', 'Show', '2024-05-18 13:50:00', '2024-05-18 13:50:00'),
(4, 7, 4, 1, 5, 'Cơm sườn ở đây đỉnh thật sự, sườn mềm, chả trứng béo ngậy.', 'Show', '2024-05-15 11:00:00', '2024-05-15 11:00:00'),
(5, 9, 8, 1, 4, 'Sườn nướng vừa tới, cơm tấm ngon. Sẽ ủng hộ tiếp.', 'Show', '2024-05-17 13:00:00', '2024-05-17 13:00:00'),
(6, 7, 4, 2, 4, 'Gà rôti da giòn, thịt mềm thấm vị, ăn với cơm nóng là tuyệt vời.', 'Show', '2024-05-15 11:05:00', '2024-05-15 11:05:00'),
(7, 10, 3, 2, 5, 'Cơm gà roti ngon xuất sắc! Gà mềm, không bị khô.', 'Show', '2024-05-14 14:00:00', '2024-05-14 14:00:00'),
(8, 8, 6, 3, 5, 'Bún chả chuẩn vị Hà Nội, chả nướng thơm lừng, nước chấm vừa miệng.', 'Show', '2024-05-16 12:00:00', '2024-05-16 12:00:00'),
(9, 10, 3, 3, 4, 'Bún chả khá ổn, nem rán (nếu có) sẽ ngon hơn.', 'Show', '2024-05-14 14:05:00', '2024-05-14 14:05:00'),
(10, 10, 3, 4, 4, 'Phở bò nước dùng đậm đà, thịt nhiều. Khá hài lòng.', 'Show', '2024-05-14 14:10:00', '2024-05-14 14:10:00'),
(11, 6, 1, 5, 5, 'Bánh mì thịt nướng ở đây là chân ái, thịt tẩm ướp ngon, vỏ bánh mì giòn.', 'Show', '2024-05-18 14:00:00', '2024-05-18 14:00:00'),
(12, 8, 6, 5, 4, 'Bánh mì đầy đặn, thịt nướng thơm. Sẽ thử lại lần sau.', 'Show', '2024-05-16 12:05:00', '2024-05-16 12:05:00'),
(13, 7, 4, 6, 3, 'Gỏi cuốn tạm được, tôm hơi nhỏ nhưng rau tươi.', 'Show', '2024-05-15 11:10:00', '2024-05-15 11:10:00'),
(14, 11, 5, 6, 4, 'Nước chấm gỏi cuốn ngon, cuốn chắc tay, vị thanh mát.', 'Show', '2024-05-13 15:00:00', '2024-05-13 15:00:00'),
(15, 8, 6, 7, 5, 'Bánh tráng trộn siêu ngon, topping đầy đủ, vị chua cay mặn ngọt hài hòa.', 'Show', '2024-05-16 12:10:00', '2024-05-16 12:10:00'),
(16, 11, 5, 7, 4, 'Bánh tráng trộn đậm đà, nhưng hơi cay với mình. Tổng thể là ngon.', 'Show', '2024-05-13 15:05:00', '2024-05-13 15:05:00'),
(17, 6, 1, 8, 4, 'Trà sữa béo ngậy, trân châu đường đen dai ngon, không quá ngọt.', 'Show', '2024-05-18 14:05:00', '2024-05-18 14:05:00'),
(18, 9, 8, 8, 5, 'Ly trà sữa chất lượng, đúng gu mình. Sẽ đặt thường xuyên.', 'Show', '2024-05-17 13:05:00', '2024-05-17 13:05:00'),
(19, 1, 2, 9, 4, 'Chè khúc bạch thanh mát, topping đa dạng, ăn giải nhiệt rất thích.', 'Show', '2024-05-20 12:30:00', '2024-05-20 12:30:00'),
(20, 7, 4, 9, 5, 'Chè khúc bạch ngon tuyệt, vị ngọt thanh, thạch mềm mịn.', 'Show', '2024-05-15 11:15:00', '2024-05-15 11:15:00'),
(21, 9, 8, 10, 5, 'Cơm chay rất ngon miệng, nhiều món, nêm nếm vừa ăn.', 'Show', '2024-05-17 13:10:00', '2024-05-17 13:10:00'),
(22, 11, 5, 10, 4, 'Đồ chay ở đây làm khéo, vị thanh đạm, tốt cho sức khỏe.', 'Show', '2024-05-13 15:10:00', '2024-05-13 15:10:00'),
(23, 1, 2, 11, 4, 'Mì Ý sốt bò bằm khá ngon, sợi mì vừa chín tới, sốt đậm đà.', 'Show', '2024-05-20 12:35:00', '2024-05-20 12:35:00'),
(24, 6, 1, 12, 5, 'Nước cam tươi vắt nguyên chất, vị chua ngọt tự nhiên, rất sảng khoái.', 'Show', '2024-05-18 14:10:00', '2024-05-18 14:10:00'),
(25, 12, 13, 2, 5, 'Cơm gà roti ngon, gà mềm, thấm vị.', 'Show', '2024-05-10 11:30:00', '2024-05-10 11:30:00'),
(26, 12, 13, 17, 4, 'Cháo sườn sụn ổn, hơi ít sụn.', 'Show', '2024-05-10 11:31:00', '2024-05-10 11:31:00'),
(27, 13, 14, 13, 5, 'Pizza hải sản siêu nhiều topping, đế bánh ngon.', 'Show', '2024-05-11 12:45:00', '2024-05-11 12:45:00'),
(28, 14, 15, 15, 4, 'Hamburger bò phô mai ngon, thịt bò chất lượng.', 'Show', '2024-05-12 18:15:00', '2024-05-12 18:15:00'),
(29, 14, 15, 19, 3, 'Cơm gà xối mỡ da chưa được giòn lắm.', 'Show', '2024-05-12 18:16:00', '2024-05-12 18:16:00'),
(30, 15, 16, 10, 5, 'Cơm chay thập cẩm ngon, đa dạng món.', 'Show', '2024-05-13 10:30:00', '2024-05-13 10:30:00'),
(31, 15, 16, 8, 4, 'Trà sữa trân châu đường đen ok, hơi ngọt.', 'Show', '2024-05-13 10:31:00', '2024-05-13 10:31:00'),
(32, 15, 16, 6, 4, 'Gỏi cuốn tươi, nước chấm vừa miệng.', 'Show', '2024-05-13 10:32:00', '2024-05-13 10:32:00'),
(33, 16, 17, 18, 5, 'Lẩu thái hải sản rất ngon, đậm đà hương vị Thái.', 'Show', '2024-05-14 20:00:00', '2024-05-14 20:00:00'),
(34, 17, 18, 19, 4, 'Cơm gà xối mỡ lần này da giòn ngon.', 'Show', '2024-05-15 13:45:00', '2024-05-15 13:45:00'),
(35, 18, 19, 20, 5, 'Ba chỉ bò nướng mềm, sốt BBQ đậm đà.', 'Show', '2024-05-16 16:30:00', '2024-05-16 16:30:00'),
(36, 18, 19, 17, 4, 'Cháo sườn nóng hổi, ăn rất ấm bụng.', 'Show', '2024-05-16 16:31:00', '2024-05-16 16:31:00'),
(37, 19, 20, 1, 4, 'Cơm sườn ngon, nhưng chả hơi khô.', 'Show', '2024-05-17 09:00:00', '2024-05-17 09:00:00'),
(38, 19, 20, 2, 5, 'Cơm gà rất tuyệt vời.', 'Show', '2024-05-17 09:01:00', '2024-05-17 09:01:00'),
(39, 19, 20, 6, 5, 'Gỏi cuốn thanh đạm, ngon.', 'Show', '2024-05-17 09:02:00', '2024-05-17 09:02:00'),
(40, 1, 2, 6, 4, 'Gỏi cuốn trong đơn này cũng ngon.', 'Show', '2024-05-20 12:32:00', '2024-05-20 12:32:00');

-- Bảng: order_status_history (Tổng cộng rất nhiều bản ghi)
INSERT INTO `order_status_history` (`history_id`, `order_id`, `status`, `changed_at`, `notes`) VALUES
(1, 1, 'Pending', '2024-05-20 11:00:00', NULL),(2, 1, 'Processing', '2024-05-20 11:05:00', 'Admin 11 xử lý'),(3, 1, 'Shipped', '2024-05-20 11:35:00', NULL),(4, 1, 'Delivered', '2024-05-20 12:10:00', 'Đã giao thành công'),
(5, 2, 'Pending', '2024-05-21 18:00:00', NULL),(6, 2, 'Processing', '2024-05-21 18:05:00', 'Admin 12 xử lý'),(7, 2, 'Shipped', '2024-05-21 18:20:00', 'Giao hàng nhanh'),
(8, 3, 'Pending', '2024-05-22 10:00:00', NULL),(9, 3, 'Processing', '2024-05-22 10:02:00', 'Admin 11 đang xử lý'),
(10, 4, 'Pending', '2024-05-22 09:50:00', NULL),
(11, 5, 'Pending', '2024-05-19 09:00:00', NULL),(12, 5, 'CancelledByUser', '2024-05-19 09:15:00', 'Người dùng yêu cầu hủy'),
(13, 6, 'Pending', '2024-05-18 12:30:00', NULL),(14, 6, 'Processing', '2024-05-18 12:35:00', 'Admin 11 xử lý'),(15, 6, 'Shipped', '2024-05-18 12:55:00', NULL),(16, 6, 'Delivered', '2024-05-18 13:30:00', 'Đã giao thành công'),
(17, 7, 'Pending', '2024-05-15 10:00:00', NULL),(18, 7, 'Processing', '2024-05-15 10:02:00', 'Admin 11 xử lý'),(19, 7, 'Shipped', '2024-05-15 10:20:00', NULL),(20, 7, 'Delivered', '2024-05-15 10:45:00', 'Đã giao'),
(21, 8, 'Pending', '2024-05-16 11:00:00', NULL),(22, 8, 'Processing', '2024-05-16 11:03:00', 'Admin 12 xử lý'),(23, 8, 'Shipped', '2024-05-16 11:25:00', NULL),(24, 8, 'Delivered', '2024-05-16 11:50:00', 'Đã giao'),
(25, 9, 'Pending', '2024-05-17 12:00:00', NULL),(26, 9, 'Processing', '2024-05-17 12:02:00', 'Admin 11 xử lý'),(27, 9, 'Shipped', '2024-05-17 12:15:00', 'Giao nhanh'),(28, 9, 'Delivered', '2024-05-17 12:40:00', 'Đã giao'),
(29, 10, 'Pending', '2024-05-14 13:00:00', NULL),(30, 10, 'Processing', '2024-05-14 13:03:00', 'Admin 12 xử lý'),(31, 10, 'Shipped', '2024-05-14 13:20:00', NULL),(32, 10, 'Delivered', '2024-05-14 13:45:00', 'Đã giao'),
(33, 11, 'Pending', '2024-05-13 14:00:00', NULL),(34, 11, 'Processing', '2024-05-13 14:02:00', 'Admin 11 xử lý'),(35, 11, 'Shipped', '2024-05-13 14:20:00', NULL),(36, 11, 'Delivered', '2024-05-13 14:45:00', 'Đã giao'),
(37, 12, 'Pending', '2024-05-10 10:00:00', NULL),(38, 12, 'Processing', '2024-05-10 10:05:00', 'Admin 11 xử lý'),(39, 12, 'Shipped', '2024-05-10 10:30:00', NULL),(40, 12, 'Delivered', '2024-05-10 11:00:00', 'Giao thành công'),
(41, 13, 'Pending', '2024-05-11 11:30:00', NULL),(42, 13, 'Processing', '2024-05-11 11:35:00', 'Admin 12 xử lý'),(43, 13, 'Shipped', '2024-05-11 11:45:00', 'Shipper đã nhận'),(44, 13, 'Delivered', '2024-05-11 12:15:00', 'Khách đã nhận hàng'),
(45, 14, 'Pending', '2024-05-12 17:00:00', NULL),(46, 14, 'Processing', '2024-05-12 17:05:00', 'Admin 11 xử lý'),(47, 14, 'Shipped', '2024-05-12 17:20:00', NULL),(48, 14, 'Delivered', '2024-05-12 17:50:00', 'Giao thành công'),
(49, 15, 'Pending', '2024-05-13 09:00:00', NULL),(50, 15, 'Processing', '2024-05-13 09:05:00', 'Admin 12 xử lý'),(51, 15, 'Shipped', '2024-05-13 09:25:00', NULL),(52, 15, 'Delivered', '2024-05-13 09:55:00', 'Giao thành công'),
(53, 16, 'Pending', '2024-05-14 19:00:00', NULL),(54, 16, 'Processing', '2024-05-14 19:02:00', 'Admin 11 xử lý'),(55, 16, 'Shipped', '2024-05-14 19:15:00', NULL),(56, 16, 'Delivered', '2024-05-14 19:40:00', 'Giao thành công'),
(57, 17, 'Pending', '2024-05-15 12:30:00', NULL),(58, 17, 'Processing', '2024-05-15 12:35:00', 'Admin 12 xử lý'),(59, 17, 'Shipped', '2024-05-15 12:50:00', NULL),(60, 17, 'Delivered', '2024-05-15 13:20:00', 'Giao thành công'),
(61, 18, 'Pending', '2024-05-16 15:00:00', NULL),(62, 18, 'Processing', '2024-05-16 15:05:00', 'Admin 11 xử lý'),(63, 18, 'Shipped', '2024-05-16 15:30:00', NULL),(64, 18, 'Delivered', '2024-05-16 16:00:00', 'Giao thành công'),
(65, 19, 'Pending', '2024-05-17 08:00:00', NULL),(66, 19, 'Processing', '2024-05-17 08:02:00', 'Admin 12 xử lý'),(67, 19, 'Shipped', '2024-05-17 08:10:00', NULL),(68, 19, 'Delivered', '2024-05-17 08:30:00', 'Giao thành công'),
(69, 20, 'Pending', '2024-05-22 09:30:00', NULL),(70, 20, 'Processing', '2024-05-22 09:35:00', 'Admin 11 đang xử lý'),
(71, 21, 'Pending', '2024-05-22 10:00:00', NULL);



SET FOREIGN_KEY_CHECKS=1;