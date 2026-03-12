package com.vanphuc;

import com.vanphuc.modules.Modules;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuTienMong implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("hutienmong");

	@Override
	public void onInitialize() {
		LOGGER.info("Đang khởi tạo hệ thống Modules...");

		// Khởi tạo bộ quản lý module
		Modules.get();

		LOGGER.info("Hệ thống đã sẵn sàng! Hẹ Hẹ 🚀");
	}
}