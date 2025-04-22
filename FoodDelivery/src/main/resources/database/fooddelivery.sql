CREATE DATABASE IF NOT EXISTS `simple_food_delivery_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `simple_food_delivery_db`;

-- Bảng: users
-- Chức năng: Lưu trữ thông tin tất cả người dùng (Khách hàng, Admin, Nhân viên nhà hàng, Tài xế)

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

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_ VARCHAR(255) NOT NULL,
    date_of_birth DATE NULL,
    phone_number VARCHAR(20) NULL UNIQUE,
    gender ENUM('Male', 'Female', 'Other') NULL,
    profile_picture_url VARCHAR(500) NULL,
    role ENUM('Customer',
              'Admin',
              'RestaurantStaff',
              'Driver'
             ) NOT NULL DEFAULT 'Customer',
    -- restaurant_id INT NULL, -- Có thể thêm nếu muốn liên kết trực tiếp NV nhà hàng
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- Bảng: restaurants
-- Chức năng: Lưu trữ thông tin các nhà hàng đối tác
DROP TABLE IF EXISTS `restaurants`;

CREATE TABLE restaurants (
    restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    phone_number VARCHAR(20) NULL,
    email VARCHAR(255) NULL UNIQUE,
    logo_url VARCHAR(500) NULL,
    operating_hours VARCHAR(255) NULL,                           -- Giờ hoạt động
    is_active BOOLEAN DEFAULT TRUE,                              -- Trạng thái hoạt động của nhà hàng (True: Đang hoạt động, False: Tạm ngưng)
    -- Có thể thêm tọa độ nếu cần tính năng bản đồ
    -- latitude DECIMAL(10, 8) NULL,                            -- Vĩ độ
    -- longitude DECIMAL(11, 8) NULL,                           -- Kinh độ
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Khóa ngoại tùy chọn: Liên kết nhân viên nhà hàng với nhà hàng cụ thể
-- ALTER TABLE users ADD CONSTRAINT fk_user_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE SET NULL;


-- Bảng: food_categories
-- Chức năng: Lưu trữ các loại món ăn (vd: Hải sản, Đồ nướng, Đồ uống, Cơm văn phòng,...)

DROP TABLE IF EXISTS `food_categories`;

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
-- Chức năng: Lưu trữ thông tin các món ăn cụ thể do nhà hàng cung cấp


CREATE TABLE foods (
    food_id INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INT NOT NULL,
    category_id INT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    availability_status ENUM('Available',     -- Còn hàng
                             'Unavailable'    -- Hết hàng
                            ) NOT NULL DEFAULT 'Available',
    image_url VARCHAR(500) NULL,
    created_by INT NULL,
    updated_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE, -- Nếu xóa nhà hàng, xóa luôn các món ăn của nhà hàng đó
    FOREIGN KEY (category_id) REFERENCES food_categories(category_id) ON DELETE SET NULL, -- Nếu xóa loại món ăn, món ăn đó sẽ không thuộc loại nào nữa (SET NULL)
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,               -- Nếu xóa người tạo, giữ lại món ăn nhưng không biết ai tạo
    FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL                -- Nếu xóa người cập nhật, giữ lại món ăn
);


-- Bảng: carts
-- Chức năng: Đại diện cho giỏ hàng (tạm thời) của người dùng tại một nhà hàng cụ thể

DROP TABLE IF EXISTS `carts`;

CREATE TABLE carts (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    restaurant_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,           -- Nếu xóa người dùng, xóa luôn giỏ hàng của họ
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE, -- Nếu nhà hàng bị xóa/ngừng hoạt động, xóa giỏ hàng liên quan
    UNIQUE KEY `user_restaurant_cart` (`user_id`, `restaurant_id`) -- Đảm bảo mỗi người dùng chỉ có 1 giỏ hàng đang hoạt động cho mỗi nhà hàng
);


-- Bảng: cart_items
-- Chức năng: Lưu các món ăn cụ thể và số lượng trong một giỏ hàng

DROP TABLE IF EXISTS `cart_items`;

CREATE TABLE cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE, -- Nếu xóa giỏ hàng, xóa luôn các mục bên trong
    FOREIGN KEY (food_id) REFERENCES foods(food_id) ON DELETE CASCADE, -- Nếu món ăn bị xóa khỏi hệ thống, xóa luôn khỏi các giỏ hàng
    UNIQUE KEY `cart_food_item` (`cart_id`, `food_id`) -- Đảm bảo không có 2 dòng cho cùng 1 món ăn trong 1 giỏ (chỉ cần cập nhật số lượng)
);


-- Bảng: addresses
-- Chức năng: Lưu trữ các địa chỉ giao hàng của người dùng

DROP TABLE IF EXISTS `addresses`;

CREATE TABLE addresses (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100) NULL,
    ward VARCHAR(100) NULL,
    address_label VARCHAR(50) NULL,                              -- Nhãn gợi nhớ cho địa chỉ (vd: "Nhà", "Công ty")
    is_default BOOLEAN DEFAULT FALSE,                            -- Địa chỉ này có phải là địa chỉ mặc định không (True/False)
    latitude DECIMAL(10, 8) NULL,                                -- Vĩ độ (hỗ trợ bản đồ và định vị)
    longitude DECIMAL(11, 8) NULL,                               -- Kinh độ (hỗ trợ bản đồ và định vị)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE -- Nếu xóa người dùng, xóa luôn các địa chỉ của họ
);


-- Bảng: drivers
-- Chức năng: Lưu trữ thông tin chi tiết và trạng thái của tài xế giao hàng

DROP TABLE IF EXISTS `drivers`;

CREATE TABLE drivers (
    driver_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    vehicle_details VARCHAR(255) NULL,
    current_latitude DECIMAL(10, 8) NULL,
    driver_status ENUM('Offline',    -- Ngoại tuyến / Nghỉ
                       'Available',  -- Sẵn sàng nhận đơn
                       'OnDelivery'  -- Đang trong quá trình giao hàng
                      ) NOT NULL DEFAULT 'Offline',
    average_rating DECIMAL(3, 2) DEFAULT NULL,                   -- Điểm đánh giá trung bình (tính toán từ bảng driver_reviews)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE -- Nếu xóa tài khoản người dùng của tài xế, xóa luôn thông tin tài xế
);


-- Bảng: orders
-- Chức năng: Lưu trữ thông tin về các đơn hàng đã được người dùng đặt

DROP TABLE IF EXISTS `orders`;

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    restaurant_id INT NOT NULL,
    driver_id INT NULL,
    delivery_address_id INT NOT NULL,
    order_code VARCHAR(20) UNIQUE NOT NULL,
    food_cost DECIMAL(12, 2) NOT NULL,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL,                        -- Tổng số tiền khách hàng phải trả (food_cost + delivery_fee - discount_amount)
    order_status ENUM('Pending',             -- Chờ nhà hàng xác nhận
                     'ConfirmedByRestaurant', -- Nhà hàng đã xác nhận
                     'Preparing',             -- Đang chuẩn bị món
                     'ReadyForPickup',        -- Món đã sẵn sàng, chờ tài xế đến lấy
                     'PickedUpByDriver',      -- Tài xế đã lấy hàng từ nhà hàng
                     'OutForDelivery',        -- Tài xế đang trên đường giao hàng
                     'Delivered',             -- Đã giao hàng thành công
                     'CancelledByUser',       -- Đơn hàng bị hủy bởi người dùng
                     'CancelledByRestaurant', -- Đơn hàng bị hủy bởi nhà hàng
                     'FailedDelivery'         -- Giao hàng thất bại (không liên lạc được KH,...)
                    ) NOT NULL DEFAULT 'Pending',
    payment_method ENUM('COD',           -- Thanh toán khi nhận hàng (Cash On Delivery)
                       'OnlineBanking', -- Chuyển khoản ngân hàng trực tuyến
                       'Card',          -- Thanh toán bằng thẻ (Visa, Master,...)
                       'EWallet'        -- Thanh toán bằng ví điện tử (Momo, ZaloPay,...)
                      ) NULL,
    payment_status ENUM('Pending',    -- Chưa thanh toán
                       'Paid',       -- Đã thanh toán
                       'Failed',     -- Thanh toán thất bại
                       'Refunded'    -- Đã hoàn tiền
                      ) NOT NULL DEFAULT 'Pending',
    special_instructions TEXT NULL,                              -- Ghi chú đặc biệt từ khách hàng cho nhà hàng/tài xế
    placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,               -- Thời gian khách hàng đặt đơn
    confirmed_at TIMESTAMP NULL,                                 -- Thời gian nhà hàng xác nhận đơn
    picked_up_at TIMESTAMP NULL,                                 -- Thời gian tài xế lấy hàng từ nhà hàng
    delivered_at TIMESTAMP NULL,                                 -- Thời gian giao hàng thành công
    estimated_delivery_time TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Sử dụng ON DELETE RESTRICT để giữ lại lịch sử đơn hàng ngay cả khi user/nhà hàng/địa chỉ bị xóa
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE RESTRICT,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE SET NULL, -- Nếu tài xế bị xóa, chỉ cần bỏ liên kết khỏi đơn hàng (đặt thành NULL)
    FOREIGN KEY (delivery_address_id) REFERENCES addresses(address_id) ON DELETE RESTRICT
);


-- Bảng: order_items
-- Chức năng: Lưu các món ăn cụ thể và số lượng, giá tại thời điểm đặt trong một đơn hàng

DROP TABLE IF EXISTS `order_items`;

CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_at_order DECIMAL(10, 2) NOT NULL,
    item_subtotal DECIMAL(12, 2) NOT NULL,                       -- Thành tiền cho mục này (quantity * price_at_order)

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE, -- Nếu xóa đơn hàng, xóa luôn các mục chi tiết bên trong
    FOREIGN KEY (food_id) REFERENCES foods(food_id) ON DELETE RESTRICT    -- Giữ lại thông tin món ăn trong đơn hàng ngay cả khi món đó bị xóa/thay đổi trong menu
);


-- Bảng: food_reviews
-- Chức năng: Lưu đánh giá của người dùng về chất lượng MÓN ĂN trong một đơn hàng cụ thể (UC010)

DROP TABLE IF EXISTS `food_reviews`;

CREATE TABLE food_reviews (
    food_review_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    food_id INT NOT NULL,
    rating SMALLINT NULL CHECK (rating >= 1 AND rating <= 5),    -- Điểm đánh giá (từ 1 đến 5 sao, có thể NULL nếu chỉ bình luận)
    comment TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE, -- Nếu xóa đơn hàng, xóa luôn đánh giá liên quan
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,    -- Nếu xóa người dùng, xóa luôn đánh giá của họ
    FOREIGN KEY (food_id) REFERENCES foods(food_id) ON DELETE CASCADE,    -- Nếu món ăn bị xóa, xóa luôn đánh giá về nó
    UNIQUE KEY `user_order_food_review` (`user_id`, `order_id`, `food_id`) -- Đảm bảo mỗi người dùng chỉ đánh giá 1 lần cho 1 món ăn trong 1 đơn hàng
);


-- Bảng: restaurant_reviews
-- Chức năng: Lưu đánh giá của người dùng về chất lượng dịch vụ của NHÀ HÀNG dựa trên một đơn hàng cụ thể

DROP TABLE IF EXISTS `restaurant_reviews`;

CREATE TABLE restaurant_reviews (
    restaurant_review_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    restaurant_id INT NOT NULL,
    rating SMALLINT NULL CHECK (rating >= 1 AND rating <= 5),    -- Điểm đánh giá (1-5 sao)
    comment TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE, -- Nếu nhà hàng bị xóa, xóa đánh giá về nó
    UNIQUE KEY `user_order_restaurant_review` (`user_id`, `order_id`) -- Đảm bảo mỗi người dùng chỉ đánh giá nhà hàng 1 lần cho mỗi đơn hàng
);


-- Bảng: driver_reviews
-- Chức năng: Lưu đánh giá của người dùng về thái độ và dịch vụ của TÀI XẾ dựa trên một đơn hàng cụ thể

DROP TABLE IF EXISTS `driver_reviews`;

CREATE TABLE driver_reviews (
    driver_review_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    driver_id INT NOT NULL,
    rating SMALLINT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE, -- Nếu tài xế bị xóa, xóa đánh giá về tài xế đó
    UNIQUE KEY `user_order_driver_review` (`user_id`, `order_id`) -- Đảm bảo mỗi người dùng chỉ đánh giá tài xế 1 lần cho mỗi đơn hàng
);


-- Bảng: order_status_history
-- Chức năng: (Tùy chọn nhưng nên có) Ghi lại lịch sử các lần thay đổi trạng thái của đơn hàng để dễ dàng theo dõi và kiểm tra

DROP TABLE IF EXISTS `order_status_history`;

CREATE TABLE order_status_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,                                 -- Trạng thái MỚI của đơn hàng sau khi thay đổi (phải khớp với giá trị ENUM trong bảng orders.order_status)
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT NULL,                                             -- Ghi chú thêm (vd: lý do hủy đơn, ID admin/nhân viên thực hiện thay đổi,...)

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE -- Nếu xóa đơn hàng, xóa luôn lịch sử trạng thái của nó
);


-- Bảng: password_resets
-- Chức năng: Lưu trữ token (mã tạm thời) dùng cho chức năng "Quên mật khẩu" / "Đặt lại mật khẩu" (UC003)

DROP TABLE IF EXISTS `password_resets`;

CREATE TABLE password_resets (
    email VARCHAR(255) NOT NULL,                                 -- Email của người dùng yêu cầu đặt lại mật khẩu (Khóa chính)
    token VARCHAR(255) NOT NULL UNIQUE,                          -- Mã token duy nhất được tạo ra và gửi qua email
    expires_at TIMESTAMP NOT NULL,                               -- Thời gian token hết hạn (vd: sau 1 giờ)

    PRIMARY KEY (email),                                         -- Giả định mỗi email chỉ có một yêu cầu đặt lại mật khẩu đang hoạt động tại một thời điểm
    FOREIGN KEY (email) REFERENCES users(email) ON DELETE CASCADE -- Nếu tài khoản người dùng bị xóa, xóa luôn yêu cầu đặt lại mật khẩu liên quan
);


-- Chỉ mục (Indexes) để Tối ưu hóa Hiệu suất Truy vấn

-- Bảng: users
CREATE INDEX idx_users_email ON users(email);                      -- Tăng tốc tìm kiếm/đăng nhập bằng email
CREATE INDEX idx_users_role ON users(role);                        -- Tăng tốc lọc người dùng theo vai trò
CREATE INDEX idx_users_full_name ON users(full_name);              -- Hỗ trợ tìm kiếm theo tên
CREATE INDEX idx_users_phone_number ON users(phone_number);        -- Hỗ trợ tìm kiếm theo số điện thoại

-- Bảng: restaurants
CREATE INDEX idx_restaurants_name ON restaurants(name);            -- Tăng tốc tìm kiếm nhà hàng theo tên
CREATE INDEX idx_restaurants_is_active ON restaurants(is_active);  -- Tăng tốc lọc nhà hàng đang hoạt động

-- Bảng: food_categories
CREATE INDEX idx_food_categories_name ON food_categories(name);    -- Tăng tốc tìm kiếm loại món ăn

-- Bảng: foods
CREATE INDEX idx_foods_restaurant_id ON foods(restaurant_id);      -- Tăng tốc tìm món ăn theo nhà hàng
CREATE INDEX idx_foods_category_id ON foods(category_id);        -- Tăng tốc tìm món ăn theo loại
CREATE INDEX idx_foods_name ON foods(name);                        -- Tăng tốc tìm món ăn theo tên
CREATE INDEX idx_foods_price ON foods(price);                      -- Tăng tốc lọc/sắp xếp món ăn theo giá
CREATE INDEX idx_foods_availability ON foods(availability_status); -- Tăng tốc lọc món ăn còn hàng/hết hàng

-- Bảng: carts
CREATE INDEX idx_carts_user_id ON carts(user_id);                  -- Tăng tốc truy vấn giỏ hàng của người dùng
CREATE INDEX idx_carts_restaurant_id ON carts(restaurant_id);      -- Tăng tốc truy vấn giỏ hàng theo nhà hàng

-- Bảng: cart_items
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);        -- Tăng tốc truy vấn các mục trong giỏ hàng
CREATE INDEX idx_cart_items_food_id ON cart_items(food_id);        -- Tăng tốc truy vấn liên quan đến món ăn trong giỏ

-- Bảng: addresses
CREATE INDEX idx_addresses_user_id ON addresses(user_id);          -- Tăng tốc truy vấn địa chỉ của người dùng
CREATE INDEX idx_addresses_is_default ON addresses(is_default);    -- Tăng tốc tìm địa chỉ mặc định

-- Bảng: drivers
CREATE INDEX idx_drivers_user_id ON drivers(user_id);              -- Tăng tốc tìm kiếm tài xế theo user_id
CREATE INDEX idx_drivers_status ON drivers(driver_status);         -- Tăng tốc tìm tài xế theo trạng thái (sẵn sàng, đang giao,...)

-- Bảng: orders
CREATE INDEX idx_orders_user_id ON orders(user_id);                -- Tăng tốc tìm đơn hàng của người dùng
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);    -- Tăng tốc tìm đơn hàng theo nhà hàng
CREATE INDEX idx_orders_driver_id ON orders(driver_id);            -- Tăng tốc tìm đơn hàng theo tài xế
CREATE INDEX idx_orders_status ON orders(order_status);            -- Tăng tốc lọc/tìm đơn hàng theo trạng thái
CREATE INDEX idx_orders_placed_at ON orders(placed_at);            -- Tăng tốc sắp xếp/lọc đơn hàng theo thời gian đặt
CREATE INDEX idx_orders_order_code ON orders(order_code);          -- Tăng tốc tìm đơn hàng theo mã đơn

-- Bảng: order_items
CREATE INDEX idx_order_items_order_id ON order_items(order_id);    -- Tăng tốc truy vấn chi tiết đơn hàng
CREATE INDEX idx_order_items_food_id ON order_items(food_id);      -- Tăng tốc truy vấn liên quan món ăn trong đơn hàng

-- Bảng đánh giá (reviews)
CREATE INDEX idx_food_reviews_order_id ON food_reviews(order_id);
CREATE INDEX idx_food_reviews_food_id ON food_reviews(food_id);
CREATE INDEX idx_restaurant_reviews_order_id ON restaurant_reviews(order_id);
CREATE INDEX idx_restaurant_reviews_restaurant_id ON restaurant_reviews(restaurant_id);
CREATE INDEX idx_driver_reviews_order_id ON driver_reviews(order_id);
CREATE INDEX idx_driver_reviews_driver_id ON driver_reviews(driver_id);

-- Bảng: order_status_history
CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id); -- Tăng tốc truy vấn lịch sử của đơn hàng
CREATE INDEX idx_order_status_history_changed_at ON order_status_history(changed_at); -- Tăng tốc sắp xếp lịch sử theo thời gian

-- Bảng: password_resets
CREATE INDEX idx_password_resets_token ON password_resets(token);   -- Tăng tốc tìm kiếm theo token
CREATE INDEX idx_password_resets_expires_at ON password_resets(expires_at); -- Hỗ trợ việc xóa token hết hạn

INSERT INTO `users` (`full_name`, `email`, `password_`, `date_of_birth`, `phone_number`, `gender`, `profile_picture_url`, `role`) VALUES
('Nguyễn Văn An', 'an.nguyen@email.com', 'an1995', '1995-03-15', '0901111222', 'Male', NULL, 'Customer'),
('Trần Thị Bình', 'binh.tran@email.com', 'binh1998', '1998-07-20', '0912222333', 'Female', 'https://example.com/avatar/binh.jpg', 'Customer'),
('Lê Minh Cường', 'cuong.le@email.com', 'cuong1992', '1992-11-01', '0987654321', 'Male', NULL, 'Customer'),
('Đỗ Gia Hân (Admin)', 'admin.hcm@foodapp.vn', 'han1990', '1990-01-05', '0333444555', 'Female', NULL, 'Admin'), -- Admin
('Phan Thanh Mai (Nhân viên)', 'mai.phan.quan1@email.com', 'mai1996', '1996-05-10', '0777888999', 'Female', NULL, 'RestaurantStaff'), -- NV Nhà hàng 1
('Bùi Văn Hùng (Nhân viên)', 'hung.bui.hbt@email.com', 'hung1993', '1993-09-25', '0944555666', 'Male', NULL, 'RestaurantStaff'), -- NV Nhà hàng 2
('Hoàng Anh Tuấn (Tài xế)', 'tuan.driver@email.com', 'tuan1994', '1994-02-18', '0888123456', 'Male', 'https://example.com/avatar/tuan.jpg', 'Driver'), -- Tài xế 1
('Trịnh Mỹ Linh (Tài xế)', 'linh.driver@email.com', 'linh1997', '1997-12-30', '0765987654', 'Female', NULL, 'Driver'), -- Tài xế 2
('Võ Thành Danh', 'danh.vo@email.com', 'danh2000', '2000-08-10', '0939123123', 'Male', NULL, 'Customer'),
('Huỳnh Ngọc Nhi', 'nhi.huynh@email.com', 'nhi2001', '2001-04-25', '0928456456', 'Female', NULL, 'Customer'),
('Lý Gia Bảo', 'bao.ly@email.com', 'bao1999', '1999-10-12', '0977112233', 'Male', NULL, 'Customer'),
('Phạm Thị Diệu (Admin)', 'admin.hn@foodapp.vn', 'dieu1988', '1988-06-08', '0366777888', 'Female', NULL, 'Admin'), -- Admin 2
('Ngô Bá Khánh (Nhân viên)', 'khanh.ngo.q3@email.com', 'khanh1997', '1997-03-18', '0918181818', 'Male', NULL, 'RestaurantStaff'), -- NV Nhà hàng 3
('Đặng Minh Khôi (Tài xế)', 'khoi.driver@email.com', 'khoi1991', '1991-11-22', '0868686868', 'Male', NULL, 'Driver'), -- Tài xế 3
('Mai Thị Thảo', 'thao.mai@email.com', 'thao2002', '2002-01-15', '0905050505', 'Female', NULL, 'Customer');

-- ----------------------------
-- Bảng: restaurants
-- ----------------------------
INSERT INTO `restaurants` (`name`, `address`, `phone_number`, `email`, `logo_url`, `operating_hours`, `is_active`) VALUES
('Phở Thìn Lò Đúc', '13 P. Lò Đúc, Phạm Đình Hổ, Hai Bà Trưng, Hà Nội', '02438212001', 'phothin@email.com', 'https://example.com/logo/phothin.png', '06:00 - 21:30', 1),
('Cơm Tấm Cali - Q1', '123 Nguyễn Trãi, P. Bến Thành, Quận 1, TP. Hồ Chí Minh', '02838123456', 'comtamcali.q1@email.com', 'https://example.com/logo/cali.png', '09:00 - 22:00', 1),
('Bún Chả Hương Liên (Obama)', '24 P. Lê Văn Hưu, Phan Chu Trinh, Hai Bà Trưng, Hà Nội', '02439434106', 'bunchaobama@email.com', NULL, '08:00 - 20:30', 1),
('Pizza 4P\'s Hai Bà Trưng', '48 P. Hai Bà Trưng, Tràng Tiền, Hoàn Kiếm, Hà Nội', '02836220500', 'pizza4ps.hbt@email.com', 'https://example.com/logo/4ps.png', '10:00 - 23:00', 1),
('The Coffee House - Q3', '200 Pasteur, P. Võ Thị Sáu, Quận 3, TP. Hồ Chí Minh', '02871078079', 'tch.q3@email.com', 'https://example.com/logo/tch.png', '07:00 - 22:00', 1),
('Bánh Mì Huynh Hoa', '26 Lê Thị Riêng, P. Bến Thành, Quận 1, TP. Hồ Chí Minh', '02839250885', 'banhmihuynhhoa@email.com', NULL, '14:00 - 23:00', 1),
('Lẩu Phan - Nguyễn Văn Cừ', 'Số 278 Nguyễn Văn Cừ, Long Biên, Hà Nội', '19002808', 'lauphan.nvc@email.com', NULL, '11:00 - 14:00, 18:00 - 23:00', 1),
('Gogi House - Đà Nẵng', 'Lô A5 Nguyễn Văn Linh, P. Nam Dương, Q. Hải Châu, Đà Nẵng', '02367300999', 'gogi.danang@email.com', 'https://example.com/logo/gogi.png', '10:00 - 22:00', 1),
('Chè Liên Đà Nẵng', '189 Hoàng Diệu, P. Nam Dương, Q. Hải Châu, Đà Nẵng', '02363566168', 'chelien.dn@email.com', NULL, '08:00 - 22:00', 1),
('Bún Bò Huế O Cương Chú Điệp', '6 Trần Thúc Nhẫn, Vĩnh Ninh, TP. Huế, Thừa Thiên Huế', '0777748586', 'bunbohueocuong@email.com', NULL, '06:00 - 11:00', 1),
('Mì Quảng Bà Mua', '19 Trần Bình Trọng, P. Phước Ninh, Q. Hải Châu, Đà Nẵng', '02363539090', 'miquangbamua@email.com', NULL, '06:30 - 21:30', 1);

-- ----------------------------
-- Bảng: food_categories
-- ----------------------------
INSERT INTO `food_categories` (`name`, `description`, `image_url`, `is_active`) VALUES
('Phở', 'Các loại phở bò, gà truyền thống', NULL, 1),
('Cơm', 'Các món cơm tấm, cơm văn phòng', NULL, 1),
('Bún', 'Các loại bún chả, bún bò, bún đậu', NULL, 1),
('Pizza', 'Các loại Pizza đế dày, đế mỏng', NULL, 1),
('Đồ uống', 'Trà, cà phê, nước giải khát', NULL, 1),
('Món Cuốn', 'Gỏi cuốn, phở cuốn, nem cuốn', NULL, 1),
('Tráng miệng', 'Chè, bánh ngọt, hoa quả', NULL, 1),
('Bánh Mì', 'Bánh mì thịt, pate, chả', NULL, 1),
('Lẩu', 'Các loại lẩu Thái, lẩu nấm, lẩu bò', NULL, 1),
('Món Nướng', 'Thịt nướng, hải sản nướng BBQ', NULL, 1),
('Mì Quảng', 'Đặc sản mì Quảng Đà Nẵng', NULL, 1);


-- ----------------------------
-- Bảng: foods
-- ----------------------------
INSERT INTO `foods` (`restaurant_id`, `category_id`, `name`, `description`, `price`, `availability_status`, `image_url`, `created_by`, `updated_by`) VALUES
-- Phở Thìn (restaurant_id=1)
(1, 1, 'Phở Bò Tái Chín', 'Nước dùng ngọt xương, thịt bò tươi ngon', 60000.00, 'Available', 'https://example.com/food/phobo.jpg', 4, 12), -- Admin 1 tạo, Admin 2 cập nhật
(1, 1, 'Phở Bò Tái Lăn', 'Thịt bò tái xào nhanh trên chảo nóng', 70000.00, 'Available', 'https://example.com/food/photailan.jpg', 4, 4),
(1, 1, 'Quẩy', 'Quẩy giòn ăn kèm phở', 10000.00, 'Available', NULL, 4, 4),
-- Cơm Tấm Cali (restaurant_id=2)
(2, 2, 'Cơm Tấm Sườn Bì Chả', 'Sườn nướng mật ong, bì, chả trứng hấp', 55000.00, 'Available', 'https://example.com/food/comtam.jpg', 5, 5), -- NV Mai tạo
(2, 2, 'Cơm Gà Xối Mỡ', 'Đùi gà chiên giòn, cơm chiên thơm ngon', 50000.00, 'Available', NULL, 5, 5),
(2, 2, 'Canh Rong Biển', 'Canh rong biển nấu thịt bằm', 15000.00, 'Available', NULL, 5, 5),
-- Bún Chả Hương Liên (restaurant_id=3)
(3, 3, 'Bún Chả Hà Nội (Suất nhỏ)', 'Chả nướng than hoa, bún, rau sống, nước chấm chua ngọt', 45000.00, 'Available', 'https://example.com/food/buncha.jpg', 6, 6), -- NV Hùng tạo
(3, 3, 'Bún Chả Hà Nội (Suất lớn)', 'Nhiều chả và bún hơn', 60000.00, 'Available', 'https://example.com/food/buncha.jpg', 6, 6),
(3, 6, 'Nem Cua Bể', 'Nem rán giòn rụm nhân thịt cua bể (1 cái)', 15000.00, 'Available', NULL, 6, 6),
-- Pizza 4P's (restaurant_id=4)
(4, 4, 'Pizza Margherita', 'Pizza cơ bản với sốt cà chua, phô mai mozzarella và lá húng quế', 180000.00, 'Available', 'https://example.com/food/margherita.jpg', 12, 12), -- Admin 2 tạo
(4, 4, 'Pizza Hải Sản Pesto Xanh', 'Tôm, mực, vẹm với sốt pesto xanh', 250000.00, 'Unavailable', 'https://example.com/food/seafoodpizza.jpg', 12, 12),
(4, 4, 'Pizza 4 Cheese', 'Pizza 4 loại phô mai hảo hạng', 220000.00, 'Available', NULL, 12, 12),
-- The Coffee House (restaurant_id=5)
(5, 5, 'Cà Phê Sữa Đá', 'Cà phê pha phin truyền thống với sữa đặc', 35000.00, 'Available', 'https://example.com/food/caphesua.jpg', 13, 13), -- NV Khánh tạo
(5, 5, 'Trà Đào Cam Sả', 'Trà đào kết hợp vị cam và sả thơm mát', 45000.00, 'Available', 'https://example.com/food/tradao.jpg', 13, 13),
(5, 7, 'Bánh Mì Que Pate', 'Bánh mì que giòn rụm với pate', 15000.00, 'Available', NULL, 13, 13),
-- Bánh Mì Huynh Hoa (restaurant_id=6)
(6, 8, 'Bánh Mì Đặc Biệt', 'Ổ bánh mì đầy đủ thịt nguội, chả, pate, bơ', 65000.00, 'Available', 'https://example.com/food/banhmihuynhhoa.jpg', 4, 4),
-- Lẩu Phan (restaurant_id=7)
(7, 9, 'Buffet Lẩu 139k', 'Buffet lẩu bò Mỹ, không giới hạn', 139000.00, 'Available', NULL, 4, 4),
(7, 9, 'Buffet Lẩu 199k', 'Thêm hải sản và nhiều loại topping hơn', 199000.00, 'Available', NULL, 4, 4),
-- Gogi House (restaurant_id=8)
(8, 10, 'Combo Nướng Xèo Xèo', 'Combo thịt ba chỉ bò Mỹ, sườn non, nạc vai heo', 399000.00, 'Available', 'https://example.com/food/gogi.jpg', 4, 4),
(8, 2, 'Cơm Trộn Hàn Quốc', 'Cơm trộn bibimbap với rau củ và thịt bò', 89000.00, 'Available', NULL, 4, 4),
-- Chè Liên (restaurant_id=9)
(9, 7, 'Chè Thái Sầu Riêng', 'Chè Thái đặc trưng với sầu riêng tươi', 35000.00, 'Available', 'https://example.com/food/chelien.jpg', 4, 4),
(9, 7, 'Chè Khúc Bạch', 'Chè khúc bạch phô mai, nhãn, hạnh nhân', 30000.00, 'Available', NULL, 4, 4),
-- Bún Bò Huế O Cương (restaurant_id=10)
(10, 3, 'Bún Bò Huế Đặc Biệt', 'Tô bún bò đầy đủ giò, chả, thịt bò, tiết', 50000.00, 'Available', 'https://example.com/food/bunbohue.jpg', 4, 4),
-- Mì Quảng Bà Mua (restaurant_id=11)
(11, 11, 'Mì Quảng Gà', 'Mì Quảng với thịt gà ta dai ngon', 40000.00, 'Available', 'https://example.com/food/miquang.jpg', 4, 4),
(11, 11, 'Mì Quảng Tôm Thịt', 'Mì Quảng với tôm và thịt heo', 45000.00, 'Available', NULL, 4, 4);

-- ----------------------------
-- Bảng: addresses
-- ----------------------------
INSERT INTO `addresses` (`user_id`, `street_address`, `city`, `district`, `ward`, `address_label`, `is_default`, `latitude`, `longitude`) VALUES
(1, 'Số 10, Ngõ 50, Đường Liễu Giai', 'Hà Nội', 'Ba Đình', 'Cống Vị', 'Nhà riêng', 1, 21.0351, 105.8190),
(1, 'Tòa nhà Lotte Center, 54 Liễu Giai', 'Hà Nội', 'Ba Đình', 'Cống Vị', 'Công ty', 0, 21.0356, 105.8202),
(2, 'Chung cư Saigon Pearl, 92 Nguyễn Hữu Cảnh', 'TP. Hồ Chí Minh', 'Bình Thạnh', 'P. 22', 'Nhà riêng', 1, 10.7883, 106.7191),
(3, 'Ký túc xá Đại học Bách Khoa HN', 'Hà Nội', 'Hai Bà Trưng', 'Bách Khoa', 'KTX', 1, 21.0061, 105.8434),
(9, '150 Duy Tân', 'Đà Nẵng', 'Hải Châu', 'Hòa Thuận Đông', 'Nhà', 1, 16.0470, 108.2179),
(10, 'Landmark 81, Vinhomes Central Park', 'TP. Hồ Chí Minh', 'Bình Thạnh', 'P. 22', 'Căn hộ', 1, 10.7945, 106.7219),
(11, 'Vincom Bà Triệu, 191 Bà Triệu', 'Hà Nội', 'Hai Bà Trưng', 'Lê Đại Hành', 'Văn phòng', 0, 21.0136, 105.8490),
(11, 'Royal City, 72 Nguyễn Trãi', 'Hà Nội', 'Thanh Xuân', 'Thượng Đình', 'Nhà', 1, 21.0000, 105.8167),
(15, '25 Ngô Quyền', 'Huế', 'TP. Huế', 'Vĩnh Ninh', 'Nhà', 1, 16.4571, 107.5881),
(5, '300 Điện Biên Phủ', 'TP. Hồ Chí Minh', 'Quận 3', 'P. Võ Thị Sáu', 'Công ty', 1, 10.7858, 106.6907);

-- ----------------------------
-- Bảng: drivers
-- ----------------------------
INSERT INTO `drivers` (`user_id`, `vehicle_details`, `current_latitude`, `driver_status`, `average_rating`) VALUES
(7, 'Honda Wave Alpha - 29H1-12345', 21.0285, 'Available', 4.85), -- Tài xế Tuấn (HN)
(8, 'Yamaha Vision - 59N3-67890', 10.7769, 'Offline', 4.70),    -- Tài xế Linh (HCM)
(14, 'Suzuki Raider - 75F1-98765', 16.4637, 'Available', 4.90); -- Tài xế Khôi (Huế/ĐN?)

-- ----------------------------
-- Bảng: carts & cart_items (Ví dụ vài giỏ hàng)
-- ----------------------------
-- Giỏ hàng 1: User An (1) mua Phở Thìn (1)
INSERT INTO `carts` (`user_id`, `restaurant_id`) VALUES (1, 1);
SET @cart1_id = LAST_INSERT_ID();
INSERT INTO `cart_items` (`cart_id`, `food_id`, `quantity`) VALUES
(@cart1_id, 1, 2), -- 2 Phở Tái Chín
(@cart1_id, 3, 4); -- 4 Quẩy

-- Giỏ hàng 2: User Bình (2) mua Cơm Tấm Cali (2)
INSERT INTO `carts` (`user_id`, `restaurant_id`) VALUES (2, 2);
SET @cart2_id = LAST_INSERT_ID();
INSERT INTO `cart_items` (`cart_id`, `food_id`, `quantity`) VALUES
(@cart2_id, 4, 1), -- 1 Cơm Gà Xối Mỡ
(@cart2_id, 6, 1); -- 1 Canh Rong Biển

-- Giỏ hàng 3: User Danh (9) mua Pizza 4P's (4)
INSERT INTO `carts` (`user_id`, `restaurant_id`) VALUES (9, 4);
SET @cart3_id = LAST_INSERT_ID();
INSERT INTO `cart_items` (`cart_id`, `food_id`, `quantity`) VALUES
(@cart3_id, 11, 1), -- 1 Pizza 4 Cheese
(@cart3_id, 10, 1); -- 1 Pizza Margherita (đã tồn tại, ví dụ) -> Lỗi UNIQUE KEY, không insert được, cần UPDATE quantity nếu logic xử lý

-- ----------------------------
-- Bảng: orders & order_items (Ví dụ vài đơn hàng)
-- ----------------------------
-- Đơn hàng 1 (Đã giao): User Bình (2) đặt Cơm Tấm Cali (2), Driver Tuấn (7) giao tới địa chỉ (3)
INSERT INTO `orders` (`user_id`, `restaurant_id`, `driver_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `confirmed_at`, `picked_up_at`, `delivered_at`, `estimated_delivery_time`) VALUES
(2, 2, 1, 3, 'FD240521A1', 65000.00, 15000.00, 0.00, 80000.00, 'Delivered', 'COD', 'Paid', 'Giao giờ hành chính, gọi trước khi đến 5 phút.', '2024-05-20 11:30:00', '2024-05-20 11:32:00', '2024-05-20 11:55:00', '2024-05-20 12:15:00', '2024-05-20 12:20:00');
SET @order1_id = LAST_INSERT_ID();
INSERT INTO `order_items` (`order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(@order1_id, 4, 1, 50000.00, 50000.00), -- 1 Cơm Gà Xối Mỡ giá 50k
(@order1_id, 6, 1, 15000.00, 15000.00); -- 1 Canh Rong Biển giá 15k

-- Đơn hàng 2 (Đang chờ xác nhận): User Cường (3) đặt Bún Chả Hương Liên (3), giao tới địa chỉ (4)
INSERT INTO `orders` (`user_id`, `restaurant_id`, `driver_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `estimated_delivery_time`) VALUES
(3, 3, NULL, 4, 'FD240522B2', 75000.00, 18000.00, 5000.00, 88000.00, 'Pending', 'EWallet', 'Pending', 'Thêm nhiều rau sống.', NOW(), DATE_ADD(NOW(), INTERVAL 45 MINUTE));
SET @order2_id = LAST_INSERT_ID();
INSERT INTO `order_items` (`order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(@order2_id, 7, 1, 45000.00, 45000.00), -- 1 Bún chả nhỏ
(@order2_id, 9, 2, 15000.00, 30000.00); -- 2 Nem cua bể

-- Đơn hàng 3 (Đang chuẩn bị): User Nhi (10) đặt The Coffee House (5), Driver Linh (8 - giả sử đã online và nhận), giao tới địa chỉ (6)
INSERT INTO `orders` (`user_id`, `restaurant_id`, `driver_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `confirmed_at`, `estimated_delivery_time`) VALUES
(10, 5, 2, 6, 'FD240522C3', 80000.00, 12000.00, 0.00, 92000.00, 'Preparing', 'Card', 'Paid', 'Ít đá, nhiều đường.', '2024-05-22 09:00:00', '2024-05-22 09:02:00', '2024-05-22 09:40:00');
SET @order3_id = LAST_INSERT_ID();
INSERT INTO `order_items` (`order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(@order3_id, 13, 1, 35000.00, 35000.00), -- 1 Cafe sữa đá
(@order3_id, 14, 1, 45000.00, 45000.00); -- 1 Trà đào cam sả

-- Đơn hàng 4 (Đã hủy bởi User): User Bảo (11) đặt Pizza 4P (4), giao tới địa chỉ (7)
INSERT INTO `orders` (`user_id`, `restaurant_id`, `driver_id`, `delivery_address_id`, `order_code`, `food_cost`, `delivery_fee`, `discount_amount`, `total_amount`, `order_status`, `payment_method`, `payment_status`, `special_instructions`, `placed_at`, `confirmed_at`, `updated_at`) VALUES
(11, 4, NULL, 7, 'FD240520D4', 400000.00, 20000.00, 20000.00, 400000.00, 'CancelledByUser', 'OnlineBanking', 'Refunded', NULL, '2024-05-20 18:00:00', '2024-05-20 18:05:00', '2024-05-20 18:15:00'); -- Cập nhật lại thời gian hủy
SET @order4_id = LAST_INSERT_ID();
INSERT INTO `order_items` (`order_id`, `food_id`, `quantity`, `price_at_order`, `item_subtotal`) VALUES
(@order4_id, 10, 1, 180000.00, 180000.00), -- 1 Pizza Margherita
(@order4_id, 12, 1, 220000.00, 220000.00); -- 1 Pizza 4 Cheese

-- ----------------------------
-- Bảng: Reviews (Cho đơn hàng đã giao - Order 1)
-- ----------------------------
INSERT INTO `food_reviews` (`order_id`, `user_id`, `food_id`, `rating`, `comment`) VALUES
(@order1_id, 2, 4, 5, 'Cơm gà giòn, rất ngon!'); -- User Bình review Cơm Gà Xối Mỡ
-- (User Bình chưa review Canh Rong Biển)

INSERT INTO `restaurant_reviews` (`order_id`, `user_id`, `restaurant_id`, `rating`, `comment`) VALUES
(@order1_id, 2, 2, 4, 'Đồ ăn ngon, đóng gói ổn. Giá hơi cao xíu.'); -- User Bình review nhà hàng Cơm Tấm Cali

INSERT INTO `driver_reviews` (`order_id`, `user_id`, `driver_id`, `rating`, `comment`) VALUES
(@order1_id, 2, 1, 5, 'Tài xế giao nhanh, nhiệt tình.'); -- User Bình review tài xế Tuấn

-- ----------------------------
-- Bảng: order_status_history (Lịch sử cho các đơn hàng)
-- ----------------------------
-- Lịch sử cho Order 1
INSERT INTO `order_status_history` (`order_id`, `status`, `changed_at`, `notes`) VALUES
(@order1_id, 'Pending', '2024-05-20 11:30:00', 'Đơn hàng mới được tạo'),
(@order1_id, 'ConfirmedByRestaurant', '2024-05-20 11:32:00', 'Nhà hàng đã xác nhận'),
(@order1_id, 'Preparing', '2024-05-20 11:33:00', NULL),
(@order1_id, 'ReadyForPickup', '2024-05-20 11:50:00', NULL),
(@order1_id, 'PickedUpByDriver', '2024-05-20 11:55:00', 'Tài xế ID 1 đã nhận đơn'),
(@order1_id, 'OutForDelivery', '2024-05-20 11:56:00', NULL),
(@order1_id, 'Delivered', '2024-05-20 12:15:00', 'Đã giao thành công, thanh toán COD');

-- Lịch sử cho Order 2
INSERT INTO `order_status_history` (`order_id`, `status`, `changed_at`, `notes`) VALUES
(@order2_id, 'Pending', NOW(), 'Đơn hàng mới được tạo');

-- Lịch sử cho Order 3
INSERT INTO `order_status_history` (`order_id`, `status`, `changed_at`, `notes`) VALUES
(@order3_id, 'Pending', '2024-05-22 09:00:00', 'Đơn hàng mới được tạo'),
(@order3_id, 'ConfirmedByRestaurant', '2024-05-22 09:02:00', 'Nhà hàng đã xác nhận'),
(@order3_id, 'Preparing', '2024-05-22 09:03:00', NULL);

-- Lịch sử cho Order 4
INSERT INTO `order_status_history` (`order_id`, `status`, `changed_at`, `notes`) VALUES
(@order4_id, 'Pending', '2024-05-20 18:00:00', 'Đơn hàng mới được tạo'),
(@order4_id, 'ConfirmedByRestaurant', '2024-05-20 18:05:00', 'Nhà hàng đã xác nhận'),
(@order4_id, 'CancelledByUser', '2024-05-20 18:15:00', 'Người dùng yêu cầu hủy đơn');

-- ----------------------------
-- Bảng: password_resets (Để trống)
-- ----------------------------