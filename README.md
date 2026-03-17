# 🌌 Hư Tiên Mộng Client — Next-Gen Fabric Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-brightgreen.svg)](https://minecraft.net/)
[![Loader](https://img.shields.io/badge/Loader-Fabric-blue.svg)](https://fabricmc.net/)
[![Design](https://img.shields.io/badge/Design-Sleek%20Carbon-121212.svg)](https://github.com/MichaelPhucs-MaiCo)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Hư Tiên Mộng** là một bản mod (Client) được phát triển trên nền tảng **Fabric 1.21.4**, hướng tới sự tối giản, hiệu năng cao và đặc biệt là giao diện **Sleek Carbon** cực kỳ sang trọng, chống mỏi mắt cho các "pháp sư" Minecraft.

---

## ✨ Tính năng nổi bật (Features)

### 🎨 Giao diện Sleek Carbon (UI/UX)
* **Modern Dashboard:** Giao diện được thiết kế theo phong cách hiện đại, bo góc mịn màng, không dùng màu Neon chói mắt.
* **Bảng màu Sleek Carbon:** Nền chính `#121212`, Surface `#1E1E1E`, và màu nhấn Xanh Blue `#0F4C81`.
* **Immersive Dark Mode:** Đồng bộ thanh tiêu đề (Title Bar) với màu đen sâu thẳm của ứng dụng thông qua Windows API.
* **Notepad List:** Tích hợp trình soạn thảo văn bản ngay trong game để cấu hình danh sách kịch bản (Scripting).

### 🛠 Hệ thống Modules
* **AutoClicker:** Vung tay chém quái với tốc độ tùy chỉnh (ms).
* **AutoSwitchHotbar:** Tự động nhảy slot theo kịch bản (ví dụ: `Slot 1 delay 5s`), hỗ trợ delay ngẫu nhiên chống bị server "gáy".
* **Baritone Integration:** Tích hợp khả năng tự động tìm đường, chạy về vị trí chốt hoặc đi theo thực thể.
* **HudTest & FPSHud:** Hiển thị thông số FPS, thời gian đếm ngược một cách tinh tế.
* **Slot Index:** Hiển thị số thứ tự slot trong rương và kho đồ (có nút bật/tắt tiện lợi).

### ⚙️ Core System
* **Event System:** Kiến trúc hướng sự kiện giúp tối ưu hóa hiệu năng và dễ dàng mở rộng.
* **Config Manager:** Tự động lưu và tải mọi thiết lập, tọa độ cửa sổ GUI qua file JSON (`hutienmong.json`).
* **Advanced Mixins:** Can thiệp sâu vào Minecraft để chỉnh sửa Tooltip, Mouse handling và Keyboard blocking khi đang mở Menu.

---

## 🛠 Hướng dẫn Build (Development)

Với môi trường đã cài đặt sẵn **JDK 21+**, việc build cực kỳ đơn giản:

1.  **Clone repository:**
    ```bash
    git clone [https://github.com/MichaelPhucs-MaiCo/hutienmong.git](https://github.com/MichaelPhucs-MaiCo/hutienmong.git)
    cd hutienmong
    ```
2.  **Build Project:**
    Sử dụng Gradle Wrapper có sẵn trong thư mục:
    ```bash
    ./gradlew build
    ```
3.  **Lấy File Jar:**
    Sau khi build xong, file mod sẽ nằm ở: `build/libs/hutienmong-1.0.0.jar`.

---

## ⌨️ Phím tắt (Keybinds) Mặc định

* **Mở ClickGUI:** Phím `` ` `` (Grave Accent - ngay dưới phím ESC).
* **Tắt GUI:** Phím `ESC`.
* **Thao tác trong GUI:**
    * `Chuột trái`: Bật/Tắt Module hoặc nhấn nút.
    * `Chuột phải`: Mở bảng cài đặt (Settings) chi tiết của Module.
    * `Kéo thả`: Di chuyển các cửa sổ Windows hoặc HUD trên màn hình.

---

## 📐 Triết lý thiết kế

> "Không chỉ là một công cụ, đó là một trải nghiệm thẩm mỹ."

* **Không Neon:** Tuyệt đối tránh các màu quá chói gây mỏi mắt.
* **Sự chuyên nghiệp:** Mọi thành phần từ checkbox đến thanh slider đều được bo góc và có hiệu ứng chuyển màu mượt mà.
* **Tiện dụng:** Hệ thống `AutoSavePaperMod` tích hợp sẵn logic State Machine chống bị kick khi tương tác kho đồ.

---

## 🤝 Tác giả (Authors)

* **Main Developer:** [Mai Cồ (MichaelPhucs)](https://github.com/MichaelPhucs-MaiCo)
* **Secondary GitHub:** [MajinBuu2k4](https://github.com/MajinBuu2k4)
* **AI Collaborator:** Gemini 3 Flash

---

## 📄 Giấy phép (License)

Dự án này được phát hành dưới giấy phép **MIT License** - Xem file [LICENSE](LICENSE) để biết thêm chi tiết.
