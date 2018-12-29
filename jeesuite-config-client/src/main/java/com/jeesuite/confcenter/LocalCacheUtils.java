/*
 * Copyright 2016-2018 www.jeesuite.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeesuite.confcenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.jeesuite.common.json.JsonUtils;
import com.jeesuite.common.util.ResourceUtils;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年12月24日
 */
public class LocalCacheUtils {

	private static String localStorageDir = ResourceUtils.getProperty("jeesuite.configcenter.local-storage-dir",
			System.getProperty("user.dir"));

	public static void write(Map<String, Object> datas) {
		try {
			File dir = new File(localStorageDir);
			if (!dir.exists())
				dir.mkdirs();
			File file = new File(dir, "config-cache.json");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fileWritter = new FileWriter(file.getName(), false);
			fileWritter.write(JsonUtils.toJson(datas));
			fileWritter.close();
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> read() {
		try {
			File dir = new File(localStorageDir);
			if (!dir.exists())
				dir.mkdirs();
			File file = new File(dir, "config-cache.json");
			if (!file.exists()) {
				return null;
			}

			StringBuilder buffer = new StringBuilder();
			InputStream is = new FileInputStream(file);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			line = reader.readLine();
			while (line != null) { 
				buffer.append(line); 
				line = reader.readLine();
			}
			reader.close();
			is.close();
			return JsonUtils.toObject(buffer.toString(), Map.class);
		} catch (Exception e) {
			
		}
		return null;
	}
}
