package com.vanphuc;

import com.vanphuc.module.Modules;
import com.vanphuc.utils.ConfigManager; // THÊM IMPORT NÀY
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuTienMong implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("hutienmong");

	@Override
	public void onInitialize() {
		LOGGER.info("Đang khởi tạo hệ thống Modules...");
		Modules.get();

		// GỌI HÀM LOAD CONFIG TẠI ĐÂY
		ConfigManager.load();

		LOGGER.info("Hư Tiên Mộng🚀");
	}
}