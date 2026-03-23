package com.vanphuc.event.events.packets;

import com.vanphuc.event.Cancellable;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;

/**
 * Lớp quản lý các sự kiện liên quan đến việc gửi/nhận gói tin (Packet) giữa Client (máy mình) và Server.
 * Đây là "trái tim" để làm các tính năng Anti-Kick, Bypass AntiCheat...
 */
public class PacketEvent {

    /**
     * Sự kiện Receive: Xảy ra khi Client NHẬN được một gói tin từ Server trả về.
     * - Kế thừa Cancellable: Khầy có thể huỷ (event.cancel()) không cho Client xử lý gói tin này.
     * - Ví dụ: Chặn gói tin ép tắt Fly từ server để tiếp tục bay.
     */
    public static class Receive extends Cancellable {
        public Packet<?> packet; // Gói tin nhận được
        public ClientConnection connection; // Thông tin kết nối mạng

        public Receive(Packet<?> packet, ClientConnection connection) {
            this.setCancelled(false); // Mặc định là cho phép nhận
            this.packet = packet;
            this.connection = connection;
        }
    }

    /**
     * Sự kiện Send: Xảy ra NGAY TRƯỚC KHI Client chuẩn bị GỬI một gói tin lên Server.
     * - Kế thừa Cancellable: Khầy có thể chặn (event.cancel()) không cho gói tin này bay lên Server, hoặc tráo đổi thông số bên trong nó.
     * - Ví dụ: Chỉnh sửa toạ độ Y trong gói di chuyển để lừa Anticheat (như cách Meteor làm).
     */
    public static class Send extends Cancellable {
        public Packet<?> packet; // Gói tin chuẩn bị gửi đi
        public ClientConnection connection;

        public Send(Packet<?> packet, ClientConnection connection) {
            this.setCancelled(false); // Mặc định là cho phép gửi
            this.packet = packet;
            this.connection = connection;
        }
    }

    /**
     * Sự kiện Sent: Xảy ra SAU KHI Client ĐÃ GỬI THÀNH CÔNG gói tin lên Server.
     * - Không kế thừa Cancellable: Vì gạo đã nấu thành cơm, gói tin đã bay đi rồi thì không rút lại được.
     * - Thường chỉ dùng để: Ghi log, theo dõi hoặc đếm số lượng packet đã gửi.
     */
    public static class Sent {
        public Packet<?> packet; // Gói tin đã gửi thành công
        public ClientConnection connection;

        public Sent(Packet<?> packet, ClientConnection connection) {
            this.packet = packet;
            this.connection = connection;
        }
    }
}