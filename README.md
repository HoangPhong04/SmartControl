# 📱 Smart Gesture Control (IoT & Sensors)

> **Bài tập 3 - Môn Mobile Nâng Cao 2026** > **Sinh viên thực hiện:** [Tên của bạn]  
> **Mã sinh viên:** [Mã SV của bạn]

Ứng dụng Android sử dụng các cảm biến phần cứng (Hardware Sensors) để nhận diện cử chỉ tay, từ đó mô phỏng việc điều khiển các thiết bị trong Nhà thông minh (Smart Home) mà không cần chạm trực tiếp vào màn hình.

---

## 🌟 Các tính năng nổi bật

Ứng dụng đáp ứng đầy đủ yêu cầu của đề bài và được tích hợp thêm **3 tính năng Sáng tạo nâng cao** nhằm mang lại trải nghiệm Smart Home thực thụ.

### 🎯 1. Tính năng cốt lõi (Core Features)
* **🔊 Điều chỉnh âm lượng bằng độ nghiêng:** Cầm điện thoại nghiêng lên/xuống để thay đổi % âm lượng hệ thống thực tế (Sử dụng `Sensor.TYPE_ACCELEROMETER` - Trục X).
* **💡 Vẫy tay bật/tắt đèn (Flashlight):** Vuốt tay lướt ngang qua camera trước để bật/tắt đèn Flash. Ứng dụng sử dụng `Sensor.TYPE_PROXIMITY` (Cảm biến tiệm cận) để đo khoảng cách < 3cm, mang lại độ chính xác tuyệt đối và loại bỏ hoàn toàn nhiễu.
* **🎵 Điều khiển Media:** Xoay cổ tay (lắc nhẹ sang trái/phải) để gửi lệnh `KEYCODE_MEDIA_NEXT` hoặc `PREVIOUS` giúp chuyển bài hát đang phát ngầm (Sử dụng `Sensor.TYPE_GYROSCOPE` - Trục Y).
* **🔌 Tích hợp IoT (Bluetooth/WiFi):** Giao diện cung cấp cổng kết nối nhanh đến cài đặt Bluetooth để ghép nối với Loa hoặc tai nghe không dây.

### 🚀 2. Tính năng sáng tạo (Bonus Features)
* **💤 Chế độ Đi ngủ (Smart Sleep Mode):** Khi nhạc đang phát và đèn đang sáng, người dùng chỉ cần **úp mặt điện thoại xuống bàn**, ứng dụng sẽ tự động Dừng nhạc và Tắt đèn (Dựa vào gia tốc trọng lực Trục Z < -8.5).
* **🎨 Đổi màu LED mô phỏng (Shake Gesture):** Lắc mạnh điện thoại (tính toán G-Force > 2.0) để gửi lệnh giả lập qua Bluetooth Socket. Hệ thống phản hồi bằng cách rung (`Vibrator`) và đổi màu nền UI sang màu ngẫu nhiên.
* **🗣️ Trợ lý giọng nói (Text-To-Speech):** Tích hợp AI đọc Tiếng Việt. Mỗi khi thực hiện thành công một cử chỉ (Bật đèn, Úp máy, Lắc máy...), ứng dụng sẽ phát âm thanh thông báo trạng thái.

---

## 📂 Cấu trúc dự án (Project Structure)

Dự án áp dụng kiến trúc tách biệt rõ ràng giữa logic Cảm biến (Hardware) và Giao diện (UI):

```text
app/src/main/java/com/yourname/smartgesturecontrol/
│
├── sensors/                      
│   ├── GestureListener.kt        # Interface định nghĩa hợp đồng cho 5 cử chỉ (Wave, Rotate, Tilt, FaceDown, Shake)
│   └── GestureSensorManager.kt   # Lõi thuật toán: Lọc nhiễu, xử lý ngưỡng (Threshold), Cooldown, và tính toán G-Force.
│
├── ui/                           
│   └── MainActivity.kt           # Giao diện chính, quản lý quyền (Camera, Audio), TextToSpeech và điều khiển Hệ thống Android.
│
└── res/layout/activity_main.xml  # UI chia làm 4 khối Card rõ ràng (Volume, Light, Media, IoT).
