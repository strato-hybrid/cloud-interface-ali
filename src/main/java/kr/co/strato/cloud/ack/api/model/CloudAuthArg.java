package kr.co.strato.cloud.ack.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudAuthArg {

	private String accessKey;
	private String accessSecret;
	private String region;

	public CloudAuthArg(String access_key, String access_secret) {
		this.accessKey = access_key;
		this.accessSecret = access_secret;
	}
	
	public boolean isNull() {
		
		if(accessKey==null || "".equals(accessKey)
				|| accessSecret==null || "".equals(accessSecret)
				) {
			
			return true;
		}
		
		return false;
	}

	
}
