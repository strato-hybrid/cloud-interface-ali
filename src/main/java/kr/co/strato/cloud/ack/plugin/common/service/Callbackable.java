package kr.co.strato.cloud.ack.plugin.common.service;

import kr.co.strato.cloud.ack.plugin.common.model.CallbackData;

public interface Callbackable {
	
	/**
	 * Cluster 작업 상태를 callback url로 전달한다.
	 * @param callable
	 * @param data
	 */
	public void sendCallback(ClusterJobCallable callbackable, CallbackData data);
}
