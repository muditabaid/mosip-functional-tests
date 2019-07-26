package io.mosip.e2e.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.mosip.registration.main.RegClient;
import io.mosip.testrunner.MosipTestRunner;

public class PacketGenerator {

	public Object[][] getPackets() {
		String pattern = "dd-MMM-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());
		String configPath = MosipTestRunner.getGlobalResourcePath() + "/packets/UniqueCBEFF_Packets";
		File file = new File(configPath);
		File[] listOfFiles = file.listFiles();
		Object[][] filePackets = new Object[listOfFiles.length][];
		int i = 0;
		for (File f : listOfFiles) {

			File[] packets = f.listFiles();

			for (File packet : packets) {
				if (packet.getName().contains(date)) {

					for (File uploadPackets : packet.listFiles()) {
						filePackets[i] = new Object[] { f.getName(), uploadPackets };

					}
					i++;
				}
			}
		}

		return filePackets;
	}

}
