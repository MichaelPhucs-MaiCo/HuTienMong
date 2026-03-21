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
-keep class meteordevelopment.orbit.** { *; }
-keep @meteordevelopment.orbit.EventHandler class * { *; }

# GIỮ MIXIN ĐỂ KHÔNG CRASH
-keep @org.spongepowered.asm.mixin.Mixin class * { *; }
-keepattributes *Annotation*,*Signature*

# LƯU Ý: ProGuard bản miễn phí KHÔNG mã hóa được String.
# Nếu muốn giấu chữ "Bật Chuột Trái", ông nên dùng thêm Skidfuscator.