# 1. XÓA SẠCH METADATA & DEBUG INFO (Cực quan trọng)
# Bỏ SourceFile và LineNumberTable sẽ khiến code không còn dòng, không còn tên file gốc
-keepattributes !SourceFile,!LineNumberTable,*Annotation*,Signature,EnclosingMethod,InnerClasses

# 2. LÀM RỐI MẠNH TAY
-dontnote
-dontwarn
-allowaccessmodification
-overloadaggressively
# Gom tất cả các class bị làm rối vào một package tên là 'a' để xóa dấu vết cấu trúc thư mục
-repackageclasses 'a'

# 3. CHỈ GIỮ LẠI NHỮNG THỨ BẮT BUỘC (Entrypoints)
# Chỉ giữ tên Class, không giữ toàn bộ phương thức bên trong nếu không cần thiết
-keep public class com.vanphuc.HuTienMong {
    public void onInitialize();
}
-keep public class com.vanphuc.HuTienMongClient {
    public void onInitializeClient();
}

# 4. MIXIN - CHỈ GIỮ LẠI NHỮNG THỨ CẦN THIẾT
# Mixin rất nhạy cảm, nhưng mình vẫn có thể làm rối các phương thức private bên trong
-keep class com.vanphuc.mixin.** { *; }
-keepattributes *Annotation*

# 5. XÓA TÊN BIẾN CỤC BỘ (Local Variable Table)
# Cái này giúp Bytecode Viewer không hiện được tên biến như 'isActivated', 'player'...
-variableskip

# 6. SỬ DỤNG TÊN "MÙ MẮT" (Tùy chọn)
# Nếu ông muốn nó hiện ra kiểu lIlIIllIl thì dùng thêm dictionary (tui sẽ hướng dẫn sau nếu cần)