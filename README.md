# 📚 Knowledge Memorization App

Ứng dụng hỗ trợ ghi nhớ kiến thức hiệu quả thông qua **Flashcard** và **Quiz**. Phù hợp với học sinh, sinh viên và người tự học muốn ôn tập kiến thức mỗi ngày một cách khoa học.

---

## 🚀 Giới thiệu

**Knowledge Memorization App** là ứng dụng học tập giúp người dùng:

- Tạo bộ **Flashcard** và **Quiz** theo từng chủ đề.
- Ôn tập kiến thức thông qua hình thức **Flashcard lật thẻ** hoặc **Quiz trắc nghiệm**.
- Giao diện hiện đại, đơn giản, dễ sử dụng.
- Đồng bộ dữ liệu người dùng với tài khoản cá nhân qua **Firebase**.

---

## 🎯 Tính năng nổi bật

### ✅ Flashcard
- Tạo, chỉnh sửa, xóa flashcard theo chủ đề.
- Giao diện lật thẻ trực quan, dễ sử dụng.

### ✅ Quiz
- Tạo các câu hỏi trắc nghiệm (nhiều lựa chọn) theo từng chủ đề.
- Tính điểm theo số câu đúng và thời gian làm bài.
- Gợi ý chủ đề cần cải thiện dựa trên kết quả bài kiểm tra.

### ✅ Hệ thống thư mục & quản lý nội dung
- Người dùng có thể tạo **Folder/Subfolder** để nhóm câu hỏi/flashcard.
- Dễ dàng đổi tên, thêm câu hỏi, hoặc xóa thư mục.

### ✅ Tài khoản và lưu trữ cá nhân
- Đăng nhập bằng **Google** hoặc **Email/Password** (Firebase Authentication).
- Dữ liệu người dùng được lưu và đồng bộ qua Firebase Realtime Database hoặc Firestore.

### ✅ Tài khoản Admin
- Admin có thể quản lý toàn bộ thư mục, người dùng và nội dung quiz/flashcard.
- Hệ thống phân quyền rõ ràng giữa **Admin** và **User**.

---

## 🖼️ Giao diện ứng dụng

### 🔐 Đăng ký / Đăng nhập / Trang chủ Admin
![image](https://github.com/user-attachments/assets/8111dd57-de65-4d3a-b882-608427e71677)

### 🧑‍💼 Giao diện quản lý của Admin
![image](https://github.com/user-attachments/assets/3ffca78e-8e1d-412e-a0af-8e018f331dc3)  
![image](https://github.com/user-attachments/assets/b722ec7f-5e7f-48f8-85d3-b6ca2c5082aa)

### 👩‍🎓 Giao diện của User
![image](https://github.com/user-attachments/assets/24c63105-0b21-405c-b7f4-7aee084b7993)  
![image](https://github.com/user-attachments/assets/c8a19a4f-64c0-4ef8-90c0-6e532edb1bf9)  
![image](https://github.com/user-attachments/assets/7dbd076b-7ea6-493a-b2aa-c85e0618fd55)  
![image](https://github.com/user-attachments/assets/c02f71ca-db90-4206-a41b-893b0224a10d)

---

## 🛠️ Công nghệ sử dụng

| Thành phần         | Công nghệ                         |
|--------------------|-----------------------------------|
| Frontend           | Kotlin + XML (Android Native)     |
| Authentication     | Firebase Authentication           |
| Realtime DB        | Firebase Realtime Database        |
| Cloud DB           | Firebase Firestore                |
| UI/UX              | Material Design Components        |
| Hiệu ứng           | Snackbar, lật flashcard, animation điểm số |

---

## ⚙️ Cài đặt & chạy thử ứng dụng

1. **Mở dự án bằng Android Studio**
2. **Kết nối Firebase**
   - Truy cập [Firebase Console](https://console.firebase.google.com/)
   - Tạo Project mới
   - Thêm `package name` của ứng dụng
   - Tải file `google-services.json` và đặt vào thư mục `app/`
3. **Bật các dịch vụ Firebase**
   - `Authentication`: bật Google và Email/Password
   - `Firestore` hoặc `Realtime Database`: tạo cấu trúc lưu trữ cơ bản
4. **Build & Run ứng dụng**
   - Có thể chạy trên máy ảo hoặc thiết bị thật

---

## 📌 Lưu ý

- Mỗi thư mục (**folder**) giới hạn tối đa **100 câu hỏi**
- Mỗi ngày nếu hoàn thành quiz **20 câu**, người dùng sẽ nhận được **phần thưởng** (ví dụ: thêm lượt spin, mở thêm câu hỏi...)

---

## 👨‍💻 Tác giả

**👤 Nhất Phương**  
Intern Developer & Designer – chuyên ngành **Truyền thông đa phương tiện**  
💼 Mục tiêu: Phát triển ứng dụng học tập thân thiện, hữu ích  
🔗 GitHub: [@nhatphuong1210](https://github.com/nhatphuong1210)

---

## 🤝 Đóng góp

Bạn có thể:

- Gửi **Issue** nếu phát hiện lỗi hoặc đề xuất cải tiến
- Gửi **Pull Request** nếu muốn đóng góp mã nguồn
- ⭐ **Star** repo để ủng hộ dự án

---

> Cảm ơn bạn đã ghé thăm!  
> Hãy trải nghiệm ứng dụng và cùng nhau hoàn thiện nó tốt hơn mỗi ngày.
