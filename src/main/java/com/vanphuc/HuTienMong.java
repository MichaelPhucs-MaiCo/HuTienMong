package com.vanphuc;

import com.vanphuc.module.Modules;
import com.vanphuc.utils.ConfigManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class HuTienMong implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("hutienmong");

	@Override
	public void onInitialize() {
		// --- ĐOẠN QUAN TRỌNG NHẤT: ĐĂNG KÝ LAMBDA FACTORY ---
		// 1. Đăng ký cho môi trường Dev (IntelliJ)
		HuTienMongClient.EVENT_BUS.registerLambdaFactory("com.vanphuc", (lookupInMethod, klass) ->
				(MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);

		// 2. Đăng ký cho môi trường đã Obfuscate (Package 'a' theo config ProGuard của Khầy)
		// Dòng này giúp fix lỗi "No registered lambda listener for 'a.U'"
		HuTienMongClient.EVENT_BUS.registerLambdaFactory("a", (lookupInMethod, klass) ->
				(MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);

		LOGGER.info("Đang khởi tạo hệ thống Modules...");
		Modules.get();

		// Load config sau khi đã đăng ký EventBus thành công
		ConfigManager.load();

		LOGGER.info("Hư Tiên Mộng đã sẵn sàng và 'thông kinh mạch' Orbit! 🚀");
	}
}