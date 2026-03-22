# SỬ DỤNG DICTIONARY ĐỂ ĐỔI TÊN MÙ MẮT
-obfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt

# XÓA SẠCH DẤU VẾT
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable,*Annotation*,Signature,EnclosingMethod,InnerClasses
-dontnote
-dontwarn
-allowaccessmodification
-overloadaggressively

# Gom tất cả vào package 'a'
-repackageclasses 'a'

# CHỈ GIỮ ENTRYPOINT CỦA FABRIC
-keep public class com.vanphuc.HuTienMong { public void onInitialize(); }
-keep public class com.vanphuc.HuTienMongClient { public void onInitializeClient(); }

# ==========================================
# ĐÃ FIX: TỐI ƯU ORBIT ĐỂ GIẢM DUNG LƯỢNG JAR
# ==========================================
# Bỏ { *; } đi để ProGuard có thể tự do xóa bỏ những class/hàm của Orbit không được dùng tới
-keep class com.vanphuc.shadow.orbit.**

# BẮT BUỘC giữ lại tất cả các hàm có gắn @EventHandler để mod không bị "điếc" (vẫn nhận được sự kiện)
-keepclassmembers class * {
    @com.vanphuc.shadow.orbit.EventHandler *;
}

# GIỮ MIXIN ĐỂ KHÔNG CRASH
-keep @org.spongepowered.asm.mixin.Mixin class * { *; }
-keepattributes *Annotation*,*Signature*

# LƯU Ý: ProGuard bản miễn phí KHÔNG mã hóa được String.
# Nếu muốn giấu chữ "Bật Chuột Trái", ông nên dùng thêm Skidfuscator.