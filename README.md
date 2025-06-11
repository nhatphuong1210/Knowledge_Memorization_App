# 📚 Knowledge Memorization App

Ứng dụng hỗ trợ ghi nhớ kiến thức hiệu quả thông qua **Flashcard** và **Quiz**. Phù hợp với học sinh, sinh viên và người tự học muốn ôn tập kiến thức mỗi ngày một cách khoa học.

---

## 🚀 Giới thiệu

**Knowledge Memorization App** là ứng dụng học tập giúp người dùng:

- Tạo bộ **Flashcard** theo chủ đề để ghi nhớ nhanh chóng.
- Tham gia các **Quiz** trắc nghiệm để kiểm tra kiến thức mỗi ngày.
- Giao diện đơn giản, dễ sử dụng.
- Đồng bộ dữ liệu người dùng với tài khoản cá nhân (Firebase).

---

## 🎯 Tính năng nổi bật

### ✅ Flashcard
- Tạo, chỉnh sửa, xóa flashcard theo từng chủ đề.
- Giao diện lật flashcard đơn giản, dễ sử dụng.
- Học theo cơ chế Spaced Repetition (lặp lại cách quãng - nếu có).

### ✅ Quiz
- Tạo câu hỏi trắc nghiệm (multiple choice) theo từng chủ đề.
- Tính điểm theo số câu đúng và thời gian làm bài.
- Gợi ý chủ đề cần cải thiện dựa trên kết quả.

### ✅ Hệ thống thư mục
- Người dùng có thể tạo **Folder** để nhóm các bộ câu hỏi/flashcard theo chủ đề.
- Hỗ trợ tạo **Subfolder**, chỉnh sửa tên và xóa nếu cần.

### ✅ Tài khoản và lưu trữ
- Đăng nhập bằng Google hoặc Email (qua Firebase Authentication).
- Lưu trữ dữ liệu người dùng cá nhân trên Firebase Realtime Database hoặc Firestore.
- Mỗi người dùng có thể quản lý danh mục học riêng.

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Frontend | Kotlin + XML (Android Native) |
| Backend & Auth | Firebase Authentication |
| Database | Firebase Realtime Database / Firestore |
| UI/UX | Material Design |
| Animation | Snackbar, hiệu ứng lật thẻ, số điểm động, v.v. |

---

## 📱 Giao diện mẫu

> *(Thêm ảnh vào thư mục `screenshots/` và thay bằng tên ảnh thật)*

- Trang chủ với thư mục câu hỏi  
  ![](screenshots/home.png)

- Flashcard lật 2 mặt  
  ![](screenshots/flashcard.png)

- Quiz theo chủ đề  
  ![](screenshots/quiz.png)

- Điểm số sau mỗi bài quiz  
  ![](screenshots/score.png)

---

## 🔧 Cài đặt & chạy thử (Android)

1. **Clone dự án**:
```bash
git clone https://github.com/nhatphuong1210/Knowledge_Memorization_App.git
cd Knowledge_Memorization_App
