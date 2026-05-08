# Booking Movie App (Ứng dụng Đặt vé Xem phim)

Booking Movie là một ứng dụng di động trên nền tảng Android được xây dựng nhằm mục đích giúp người dùng dễ dàng tìm kiếm phim, chọn rạp, chọn ghế ngồi, đặt đồ ăn nhẹ và thanh toán vé xem phim một cách tiện lợi. 

Ứng dụng được thiết kế với giao diện hiện đại, áp dụng các best practices trong phát triển Android, mang lại trải nghiệm mượt mà và trực quan cho người dùng.

## 🌟 Các chức năng chính (Features)

1. **Xác thực người dùng (Authentication)**:
   - Đăng nhập và Đăng ký tài khoản an toàn thông qua Firebase Authentication.
2. **Khám phá phim (Movies Discovery)**:
   - Xem danh sách các bộ phim đang chiếu hoặc sắp ra mắt.
   - Xem chi tiết thông tin phim (Mô tả, đạo diễn, diễn viên, thời lượng...).
3. **Chọn rạp và suất chiếu (Cinema & Showtime Selection)**:
   - Lựa chọn các rạp chiếu phim trong hệ thống.
   - Xem và chọn suất chiếu phù hợp.
4. **Đặt ghế ngồi (Seat Selection)**:
   - Giao diện trực quan cho phép người dùng xem sơ đồ phòng chiếu và chọn ghế trống mong muốn.
5. **Đặt đồ ăn và thức uống (Food & Beverage)**:
   - Cho phép chọn trước bắp, nước hoặc các combo đồ ăn kèm.
6. **Quản lý vé (Ticket Management)**:
   - Quản lý các vé xem phim đã đặt.
   - Xem chi tiết thông tin vé, tích hợp mã QR (QR Code) để nhân viên rạp quét dễ dàng khi vào cửa.
7. **Quét mã QR (QR Scanner)**:
   - Tích hợp tính năng quét mã vạch / mã QR (thông qua ZXing) (có thể dùng để check-in vé hoặc tương tác khác).
8. **Thông báo đẩy (Push Notifications)**:
   - Nhận thông báo nhắc nhở suất chiếu hoặc khuyến mãi qua Firebase Cloud Messaging.
9. **Thống kê / Đánh giá**:
   - Sử dụng thư viện AnyChart để vẽ các biểu đồ thống kê (số lượng vé, chi tiêu, v.v.).

## 🛠 Các công nghệ và thư viện sử dụng (Tech Stack)

Dự án được xây dựng trên ngôn ngữ **Kotlin** và áp dụng kiến trúc **MVVM** (Model-View-ViewModel), kết hợp với các công nghệ và thư viện hiện đại:

### 1. Kiến trúc & Core Android
- **Kotlin**: Ngôn ngữ lập trình chính.
- **MVVM Architecture**: Tách biệt logic giao diện và dữ liệu (Sử dụng `ViewModel`, `LiveData`, `Repository`).
- **ViewBinding**: Tương tác với các thành phần UI một cách an toàn và tối ưu (thay thế cho `findViewById`).
- **Coroutines**: Xử lý các tác vụ bất đồng bộ (Network calls, Database operations).
- **Navigation Components**: Quản lý điều hướng giữa các màn hình (Activity, Fragment, ViewPager2).

### 2. Giao diện (UI/UX)
- **Material Design**: Hệ thống thiết kế chuẩn từ Google.
- **Facebook Shimmer**: Hiệu ứng loading dạng "shimmer" (xương) để tăng trải nghiệm người dùng khi đang tải dữ liệu.
- **DotsIndicator**: Hiển thị dấu chấm chỉ mục cho các banner dạng ViewPager.
- **SDP & SSP (Scalable DP/SP)**: Thư viện hỗ trợ tự động scale kích thước UI (margin, padding, text size) trên nhiều thiết bị khác nhau.
- **Glide**: Tải và hiển thị hình ảnh tối ưu từ Internet (Poster phim, ảnh đồ ăn, avatar...).
- **AnyChart-Android**: Vẽ biểu đồ, đồ thị trực quan trên ứng dụng.

### 3. Backend & Cơ sở dữ liệu đám mây (Cloud Services)
Dự án sử dụng toàn diện hệ sinh thái **Firebase**:
- **Firebase Authentication**: Quản lý tài khoản và phiên đăng nhập.
- **Firebase Cloud Firestore**: Cơ sở dữ liệu NoSQL lưu trữ thông tin phim, rạp, user, lịch sử đặt vé theo thời gian thực.
- **Firebase Cloud Messaging (FCM)**: Gửi thông báo đẩy (Push Notifications) đến thiết bị người dùng.
- **Firebase Remote Config**: Quản lý các cài đặt tùy chỉnh và thay đổi cấu hình app từ xa.

### 4. Kết nối mạng & API (Networking)
- **Retrofit2**: REST API Client mạnh mẽ để giao tiếp với server từ xa.
- **OkHttp3 & Logging Interceptor**: Quản lý các request HTTP và in log (header, body) để dễ dàng debug lỗi mạng.
- **Gson**: Chuyển đổi dữ liệu JSON từ API thành các object Kotlin.

### 5. Tính năng nâng cao (Advanced Features)
- **ZXing (Zebra Crossing) & ZXing Android Embedded**: Tích hợp camera để quét mã QR/Barcode tiện lợi (cần xin quyền Camera).

## 📂 Cấu trúc thư mục (Architecture / Package Structure)
\`\`\`
app/src/main/java/com/example/film/
├── base/        # Các lớp Base (BaseActivity, BaseFragment) giúp giảm thiểu boilerplate code
├── custom/      # Các View, Component UI được custom lại cho phù hợp dự án
├── database/    # Lớp cấu hình và truy xuất dữ liệu
├── model/       # Các Data Class (UserModel, BookingModel, FoodItem...)
├── repository/  # Nơi xử lý luồng lấy dữ liệu (từ API hoặc Firebase)
├── ui/          # Giao diện người dùng
│   ├── activity/  # LoginActivity, MainActivity, BookingActivity, SeatCinemaActivity...
│   ├── fragment/  # ChooseFilmFragment, ChooseFoodFragment, MyTicketFragment, SettingFragment...
│   └── adapter/   # Các Adapter để hiển thị danh sách lên RecyclerView
├── utils/       # Các hàm tiện ích, hằng số, Firebase Service, Extention functions
└── viewmodel/   # Các lớp ViewModel giữ và xử lý logic hiển thị dữ liệu
\`\`\`

## 🚀 Hướng dẫn chạy dự án (How to run)

1. Mở phần mềm **Android Studio**.
2. Chọn **Open an Existing Project** và trỏ tới thư mục mã nguồn.
3. Đợi Gradle đồng bộ hóa các thư viện (Sync Project with Gradle Files).
4. Thêm file \`google-services.json\` (của Firebase) vào thư mục \`app/\` nếu dự án chưa có hoặc bạn muốn trỏ sang Firebase của riêng mình.
5. Kết nối thiết bị Android thật hoặc khởi động máy ảo (Emulator).
6. Nhấn nút **Run** (mũi tên xanh) để cài đặt và trải nghiệm ứng dụng.

---
*Dự án Booking Movie là một minh chứng hoàn chỉnh cho việc xây dựng ứng dụng thương mại điện tử kết hợp quản lý đặt chỗ trên Android bằng ngôn ngữ Kotlin.*
