package kr.co.strato.cloud.ack.api.service;

import kr.co.strato.cloud.ack.api.model.RequestParamDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ParamValidationService {
	private String kubernetesVersion; //쿠버네티스 버전
	private String workerInstanceType; //노드 인스턴스 타입 ID
	private String vpcId; //VPC 네트워크 ID
	private String region_id; //region ID
	private String vswitchIds; //기본 워커 노드 그룹 적용 : 가용성 영역
	private String loadBalancerSpec; //로드밸런스 스펙
	private String templateName;// osType
	private String serviceCidr;
	private String diskCategory;
	private String nodepoolName;
	/**
	 * null 값을 Default 값으로 치환.
	 */
	public void ofDefault(RequestParamDto.Create param) {

		if(param.getKubernetesVersion() == null) param.setKubernetesVersion(kubernetesVersion);
		if(param.getWorkerInstanceType() == null) param.setWorkerInstanceType(workerInstanceType);
		if(param.getVpcId() == null) param.setVpcId(vpcId);
		if(param.getRegionId() == null) param.setRegionId(region_id);
		if(param.getVswitchIds() == null) param.setVswitchIds(vswitchIds);
		if(param.getLoadBalancerSpec()==null) param.setLoadBalancerSpec(loadBalancerSpec);
		if(param.getNodepoolName()==null) param.setNodepoolName(nodepoolName);
		if(param.getTemplateName()== null) param.setTemplateName(templateName);
		if(param.getServiceCidr()== null) param.setServiceCidr(serviceCidr);
		if(param.getDiskCategory()==null) param.setDiskCategory(diskCategory);

		log.debug("[ofDefault] >>> default setting param = {}", param);
		
	}
		
}
