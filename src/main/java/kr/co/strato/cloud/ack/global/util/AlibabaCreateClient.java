package kr.co.strato.cloud.ack.global.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import kr.co.strato.cloud.ack.api.model.CloudAuthArg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class AlibabaCreateClient {

	private String accessKey;

	private String accessSecret;

	private String region;


	private String authFilePath;

	public Map<String, Object> getACKAuth(CloudAuthArg authArg, String region) {

		log.debug("[getACKAuth] start");

		// 인증 데이터 선택(1. header, 2. file, 3. default)

		String getAccessKey = null;
		String getAccessSecret = null;
		if(authArg != null){
			getAccessKey = authArg.getAccessKey();
			getAccessSecret = authArg.getAccessSecret();
		}else{
			authArg = getAuthType(authArg);
			getAccessKey = authArg.getAccessKey();
			getAccessSecret = authArg.getAccessSecret();
		}

		com.aliyun.teaopenapi.models.Config config =  new  com.aliyun.teaopenapi.models.Config();
		config.accessKeyId = getAccessKey;
		config.accessKeySecret = getAccessSecret;
		config.regionId = region;
		com.aliyun.cs20151215.Client client = null;


		try {
			client = new com.aliyun.cs20151215.Client(config);

		} catch (Exception e) {
			log.error("[getACKAuth] ack auth fail", e);
		}

		Map<String, Object> principalProfile = new HashMap<>();

		principalProfile.put("client", client);
		principalProfile.put("accessKey", getAccessKey);
		principalProfile.put("accessSecret", getAccessSecret);

		log.debug("[getACKAuth] >>> return = {}", principalProfile.toString());

		return principalProfile;

	}

	public CloudAuthArg setACKAuth(CloudAuthArg arg) {

		log.debug("[setAckAuth] start");

		String setAccessKey = Optional.ofNullable(arg.getAccessKey()).orElse(accessKey);
		String setAccessSecret = Optional.ofNullable(arg.getAccessSecret()).orElse(accessSecret);
		String setRegion = Optional.ofNullable(arg.getRegion()).orElse(region);

		com.aliyun.cs20151215.Client client = null;

		try {


			com.aliyun.teaopenapi.models.Config config =  new  com.aliyun.teaopenapi.models.Config();
			config.accessKeyId = setAccessKey;
			config.accessKeySecret = setAccessSecret;
			config.regionId = setRegion;
			client = new com.aliyun.cs20151215.Client(config);

		} catch (Exception e) {
			log.error("[setAckAuth] ack auth fail", e);
		}

		CloudAuthArg authArg = new CloudAuthArg(setAccessKey, setAccessSecret);

		log.debug("[getACKAuth] >>> return = {}", authArg.toString());

		return authArg;
	}


	public CloudAuthArg getAuthType(CloudAuthArg arg) {

		Gson gson = new Gson();
		JsonReader jsonReader = null;
		JsonObject jsonObject = null;

		/**
		 * 인증방식 우선순위
		 * 1. header auth data 로 인증
		 * 2. file auth data 로 인증
		 * 3. default data 로 인증
		 */
		if(arg == null || arg.isNull()) {

			try {
				jsonReader = new JsonReader(new FileReader(authFilePath + "auth_info.json"));
				jsonObject = gson.fromJson(jsonReader, JsonObject.class);

				arg.setAccessKey(Optional.ofNullable(jsonObject.get("access_key").getAsString()).orElse(accessKey));
				arg.setAccessSecret(Optional.ofNullable(jsonObject.get("access_secret").getAsString()).orElse(accessSecret));

				log.info("[setAuthType] >>> file data auth => {}", arg.toString());

			} catch (IOException e) {
				arg = new CloudAuthArg();
				arg.setAccessKey(accessKey);
				arg.setAccessSecret(accessSecret);

				log.info("[setAuthType] >>> default data auth => {}", arg.toString());
			}
		} else {
			log.debug("[setAuthType] >>> header data auth => {}", arg.toString());
		}
		return arg;

	}

}
