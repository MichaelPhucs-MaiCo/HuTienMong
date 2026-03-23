package com.vanphuc.event.events.world;

/**
 * Lớp chứa các sự kiện liên quan đến Tick (nhịp thời gian) của game.
 * Minecraft chạy ở tốc độ 20 ticks/giây (TPS). Mỗi tick game sẽ cập nhật vị trí, logic...
 */
public class TickEvent {

    /**
     * Sự kiện Pre-Tick: Xảy ra NGAY TRƯỚC khi game bắt đầu xử lý một tick mới.
     * Thường dùng để: Cập nhật góc nhìn (yaw/pitch) giả, chuẩn bị dữ liệu di chuyển...
     */
    public static class Pre extends TickEvent {
        private static final Pre INSTANCE = new Pre();

        public static Pre get() {
            return INSTANCE;
        }
    }

    /**
     * Sự kiện Post-Tick: Xảy ra NGAY SAU khi game đã xử lý xong một tick.
     * Thường dùng để: Dọn dẹp dữ liệu, reset biến đếm, hoặc thực hiện hành động dựa trên kết quả của tick vừa rồi (ví dụ reset tick chống kick).
     */
    public static class Post extends TickEvent {
        private static final Post INSTANCE = new Post();

        public static Post get() {
            return INSTANCE;
        }
    }
}