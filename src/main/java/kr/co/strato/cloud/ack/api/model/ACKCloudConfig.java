package kr.co.strato.cloud.ack.api.model;

import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ACKCloudConfig {
	private String serverUrl;
	private String username;
	private String password;
	
	public boolean isNull() {
		
		if(serverUrl == null 			|| "".equals(serverUrl) 
				|| username==null 		|| "".equals(username)
				|| password==null		|| "".equals(password)) {
			
			return true;
		}
		return false;
	}
	
	public static ACKCloudConfig getInstance(Map<String, Object> header) {
		ACKCloudConfig config = new ACKCloudConfig();
		if(header != null) {
			String serverUrl = (String)header.get("serverUrl");
			String username = (String)header.get("username");
			String password = (String)header.get("password");			
			
			config.setServerUrl(serverUrl);
			config.setUsername(username);
			config.setPassword(password);
		}
		return config;
	}	
	
	public static ACKCloudConfig getInstance(HttpHeaders header) {
		ACKCloudConfig config = new ACKCloudConfig();
		if(header != null) {
			String serverUrl = (String)header.getFirst("serverUrl");
			String username = (String)header.getFirst("username");
			String password = (String)header.getFirst("password");
			
			config.setServerUrl(serverUrl);
			config.setUsername(username);
			config.setPassword(password);
		}
		return config;
	}
	
	public static ACKCloudConfig getDefault(Environment environment) {
		ACKCloudConfig config = new ACKCloudConfig();
		if(environment != null) {
			
			String serverUrl = environment.getProperty("default.ack.serverUrl");
			String username = environment.getProperty("default.ack.username");
			String password = environment.getProperty("default.ack.password");
			
			
			config.setServerUrl(serverUrl);
			config.setUsername(username);
			config.setPassword(password);
		}
		return config;
	}
}
