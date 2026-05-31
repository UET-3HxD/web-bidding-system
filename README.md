markdown
# 🚀 Web Bidding System - Online Auction Platform

[![Build Status](https://img.shields.io/github/actions/workflow/status/UET-3hxd/web-bidding-system/maven.yml?branch=main)](https://github.com/UET-3hxd/web-bidding-system/actions)
[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html)
[![Framework](https://img.shields.io/badge/UI-JavaFX%2021-blue.svg)](https://openjfx.io/)
[![Build Tool](https://img.shields.io/badge/Build-Maven-critical.svg)](https://maven.apache.org/)

## 📝 Giới thiệu dự án
Dự án **Bài tập lớn môn Lập trình nâng cao (UET.CS2043_11)** tại **Trường Đại học Công nghệ (VNU-UET)**. Hệ thống cho phép người dùng tham gia đấu giá trực tuyến các mặt hàng trong thời gian thực, hỗ trợ các tính năng tự động nâng cao và xử lý đồng thời.

---

## 👥 Thành viên nhóm 3HxD
Dưới đây là danh sách các thành viên và phân công nhiệm vụ cụ thể:

| Họ và Tên              | MSSV     | Vai trò chính | GitHub                                     |
|:-----------------------|:---------| :--- |:-------------------------------------------|
| **Nguyễn Minh Đức**    | 25021734 | **Team Leader**, Socket, Concurrency | [@ducnm-cs](https://github.com/ducnm-cs)   |
| **Trần Lê Việt Hoàng**      | 25021778 | UI/UX Designer (JavaFX, CSS) | [@vhoang0502](https://github.com/vhoang0502) |
| **ĐInh Quốc Huy** | 25021794 | OOP Modeling, Logic Nghiệp vụ | [@DqHuY07](https://github.com/DqHuY07) |
| **Phạm Huy Hiệu**      | 25021771 | Unit Testing, Auto-bidding Logic | [@h-hieu-code](https://github.com/h-hieu-coder) |

---

## 🛠️ Công nghệ sử dụng
- **Ngôn ngữ:** Java 25 (LTS)
- **Giao diện:** JavaFX 21 & Scene Builder
- **Build Tool:** Maven (Quản lý dependencies)
- **Giao tiếp:** TCP Socket (Client-Server Architecture)
- **Kiểm thử:** JUnit 5
- **Cơ sở dữ liệu:** MySQL
- **Định dạng truyền tải:** Custom plain text

---

## 📂 Cấu trúc thư mục (MVC Architecture)
Dự án được tổ chức theo mô hình **Model-View-Controller** để đảm bảo tính dễ bảo trì và mở rộng:
```text
src/main/java/com/auction/team3hxd/
 ├── model/          # Thực thể: User, Item, Bid, Auction...
 ├── controller/     # Điều phối logic giữa UI và Model
 ├── util/           # Tiện tích (DBConnection, Config,...)
 ├── network/        # Xử lý Socket (Server/Client Handlers)
 ├── services/       # Logic nghiệp vụ: Auto-bid, Validator...
 └── App.java        # Điểm khởi chạy ứng dụng (Main)

src/main/resources/
 ├── fxml/           # Các file giao diện thiết kế từ Scene Builder
 ├── sql/               
 ├── config/
 └── styles/         # CSS tùy chỉnh cho JavaFX
 
src/test/java/com/auction/team3hxd/
 ├── services/       # Kiểm thử chức năng của services và util
 ├── util/
```

---

## 🚀 Hướng dẫn cài đặt và Chạy
### Yêu cầu hệ thống:
- Đã cài đặt **JDK 25**.
- Đã cài đặt **Maven**.

### Các bước thực hiện:
1. **Clone Repository:**
   ```bash
   git clone [https://github.com/UET-3hxd/web-bidding-system.git](https://github.com/UET-3hxd/web-bidding-system.git)
   cd web-bidding-system
   ```

2. **Biên dịch dự án:**
   ```bash
   mvn clean install
   ```

3. **Chạy ứng dụng:**
    - **Khởi động Server:**
      ```bash
      mvn exec:java -Dexec.mainClass="com.auction.ServerApp"
      ```
    - **Khởi động Client:**
      ```bash
      mvn javafx:run
      ```

---

## 📅 Lộ trình phát triển (Roadmap)
- [x] **Tuần 1-2:** Thiết lập Repository, cấu trúc Maven, thiết kế Class Diagram sơ bộ.
- [ ] **Tuần 3-5:** Xây dựng giao diện Login/Register và kết nối Socket cơ bản.
- [ ] **Tuần 6-8:** Hiện thực hóa logic đấu giá, Auto-bid và xử lý tranh chấp giá (Concurrency).
- [ ] **Tuần 9-10:** Hoàn thiện Unit Test, tối ưu hiệu năng và viết báo cáo cuối kỳ.

---
*Dự án này tuân thủ các quy định về học thuật và cam kết đóng góp mã nguồn minh bạch trên GitHub.*

## Danh sách chức năng đã hoàn thành
- Đăng nhập, đăng ký và quản lý người dùng
- Đăng ký tài khoảng admin để quản lý người dùng và xét duyệt sản phẩm
- Tạo phiên đấu giá
- Tham giá đấu giá và kéo dài phiên đấu giá
- Cập nhật trạng thái đấu giá theo thời gian thực cho client
- Hỗ trợ tạo sản phẩm theo danh muc : "ART" , "VEHICLE" , "ELECTRONIC"
- Hiển thị biểu đồ đường giá
- Cung cấp giao diện JavaFX cho client và admin
- Lưu trữ thông tin bằng MySQL, thêm/sửa/xóa thông tin qua các lớp DAO

## Link báo cáo PDF
https://drive.google.com/file/d/1tTjDG4PeOCwbE2qCR5myq7rCZjWzXKph/view?usp=sharing
## Video demo chức năng hệ thống 