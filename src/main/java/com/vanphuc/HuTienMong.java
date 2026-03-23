package com.vanphuc;

import com.vanphuc.module.Modules;
import com.vanphuc.utils.ConfigManager; // THÊM IMPORT NÀY
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class HuTienMong implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("hutienmong");

	@Override
	public void onInitialize() {
		// 1. Dạy Orbit trước (BẮT BUỘC ĐỂ ĐẦU TIÊN)
		HuTienMongClient.EVENT_BUS.registerLambdaFactory("com.vanphuc", (lookupInMethod, klass) ->
				(MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);

		LOGGER.info("Đang khởi tạo hệ thống Modules...");
		Modules.get();

		// 2. Load config (Lúc này có subscribe thoải mái cũng không sợ crash)
		ConfigManager.load();

		LOGGER.info("Hư Tiên Mộng đã sẵn sàng và 'thông kinh mạch' Orbit! 🚀");
	}
}