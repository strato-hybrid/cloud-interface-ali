package kr.co.strato.cloud.ack.plugin.vsphere.service;

import kr.co.strato.cloud.ack.api.model.RequestParamDto;
import kr.co.strato.cloud.ack.api.service.ACKInterfaceService;
import kr.co.strato.cloud.ack.global.config.ApplicationContextProvider;
import kr.co.strato.cloud.ack.plugin.common.service.Callbackable;
import kr.co.strato.cloud.ack.plugin.common.service.ClusterJobCallable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScaleJobCallable extends ClusterJobCallable<Object> {
	
	private RequestParamDto.Scale arg;
	
	public ScaleJobCallable(Callbackable callbackable, String clusterName, String callbackUrl, RequestParamDto.Scale arg) {
		super(callbackable, clusterName, callbackUrl);
		this.arg = arg;
	}
	
	@Override
	public Object clusterJob() throws Exception {
		
		log.debug("[ScaleClusterJob] >> Request Param = {}", arg.toString());

		ACKInterfaceService interfaceService = ApplicationContextProvider.getBean(ACKInterfaceService.class);
		boolean result = interfaceService.scaleCluster(arg);

		log.debug("[ScaleClusterJob] >> result = {}", result);
		return result;
	}

}
