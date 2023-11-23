package kr.co.strato.cloud.ack.api.controller;

import java.util.List;

import javax.validation.Valid;

import kr.co.strato.cloud.ack.api.model.CloudAuthArg;
import kr.co.strato.cloud.ack.api.service.ParamValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import kr.co.strato.cloud.ack.api.model.CloudResponseDto.GetList;
import kr.co.strato.cloud.ack.api.model.RequestParamDto;
import kr.co.strato.cloud.ack.api.model.ACKCloudConfig;
import kr.co.strato.cloud.ack.api.service.ACKInterfaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/v1/cloud/ack")
@RestController
public class ACKController {

	@Autowired
	ACKInterfaceService ackInterfaceService;

	@Autowired
	ParamValidationService paramValidationService;
	
	/**
	 * 클러스터 프로비저닝
	 * @param arg
	 * 		: 클러스터 생성 정보 
	 * @return
	 * 		: Admin용 KubeConfig
	 */
	@ApiOperation(value="클러스터 생성.",
		notes=""
			+"***파라메타 설명***\n"
			+"```\n"
			+"클러스터 이름			clusterName (필수)\r\n"
			+"템플릿 이름			templateName (필수)\r\n"
			+"컨트롤플레인 IP		controlPlaneIp(필수)\r\n"
			+"마스터 노드 스팩		masterSpec (옵션)\r\n"
			+"워커 노드 스팩       workerSpec (필수)\r\n"
			+"```\n"
			
			+"***ex)***\n"
			+"```\n"			
			+"{\n"
			+"	  \"clusterName\": \"vSphere-cluster\",\n"
			+"	  \"controlPlaneIp\": \"10.10.20.180\",\n"
			+"	  \"templateName\": \"ubuntu-2004-kube-v1.25.7\",\n"
			+"	  \"masterSpec\": {\n"
			+"	    \"cpu\": 2,\n"
			+"	    \"memory\": 2048,\n"
			+"	    \"nodeCount\": 1,\n"
			+"	    \"storage\": 20\n"
			+"	  },\n"
			+"	  \"workerSpec\": {\n"
			+"	    \"cpu\": 2,\n"
			+"	    \"memory\": 2048,\n"
			+"	    \"nodeCount\": 1,\n"
			+"	    \"storage\": 20\n"
			+"	  }\n"
			+"}\n"
			+"```\n"
	)
	@PostMapping("/provisioning")
	public String provisioningCluster(@RequestHeader HttpHeaders headers, @RequestBody @Valid RequestParamDto.Create arg) throws Exception {
		
		log.debug("[provisioningCluster] param = {}", arg.toString());
		CloudAuthArg authArg = new CloudAuthArg(headers.getFirst("access_key"), headers.getFirst("access_secret"));

		//default 값 셋팅
		paramValidationService.ofDefault(arg);
		String kubeConfig =
				ackInterfaceService.provisioningCluster(authArg, arg);
		log.debug("[provisioningCluster] return = {}", kubeConfig);
		
		return kubeConfig;
	}
	
	
	/**
	 * 클러스터 삭제.
	 * @param clusterName
	 * 		: 삭제하려는 클러스터 아이디
	 * @return
	 * 		: 삭제 성공 여부.
	 */
	@ApiOperation(value="클러스터 삭제.",
		notes=""
			+"***파라메타 설명***\n"
			+"```\n"
			+"클러스터 아이디			clusterId (필수)\r\n"
			+"```\n"
	)
	@DeleteMapping("/delete")
	public boolean deleteCluster(@RequestHeader HttpHeaders headers, @RequestBody @Valid RequestParamDto.Delete arg) throws Exception {
		
		log.debug("[deleteCluster] param = {}", arg.toString());
		CloudAuthArg authArg = new CloudAuthArg(headers.getFirst("access_key"), headers.getFirst("access_secret"));
		boolean result = ackInterfaceService.deleteCluster(authArg, arg);
		log.debug("[deleteCluster] return = {}", result);
		
		return result;
	}
	
	
	/**
	 * 노드 스케일 조정
	 * @param arg
	 * @return
	 * 		: 스케일 조정 성공 여부
	 */
	@ApiOperation(value = "노드 스케일 조정")
	@PostMapping("/scale")
	public boolean scaleCluster(@RequestHeader HttpHeaders headers, @RequestBody @Valid RequestParamDto.Scale arg) {
		
		log.debug("[scaleCluster] param = {}", arg.toString());	
		ACKCloudConfig config = ACKCloudConfig.getInstance(headers);
		boolean result = ackInterfaceService.scaleCluster(config, arg);
		log.debug("[scaleCluster] return = {}", result);
		
		return result;
	}
	
	
	
	/**
	 * 클러스터 중복 체크
	 * @param clusterName
	 * 		: 중복체크하려는 클러스터 이름
	 * @return
	 * 		: 중복 여부.
	 */
	@ApiOperation(value = "클러스터 중복 체크")
	@PostMapping("/duplicateCheck")
	public boolean duplicateCheckCluster(@RequestHeader HttpHeaders headers, @RequestBody RequestParamDto.Duplicate arg) {
		
		log.debug("[duplicateCheckCluster] param = {}", arg.toString());
		ACKCloudConfig config = ACKCloudConfig.getInstance(headers);
		boolean result = ackInterfaceService.duplicateCheckCluster(config, arg);
		log.debug("[duplicateCheckCluster] return = {}", result);
		
		return result;
	}
	
	@ApiOperation(value = "클러스터 리스트 조회")
	@GetMapping("/api/v1/cloud/ack/list")
	public List<GetList> getListCluster(@RequestHeader HttpHeaders headers, String region) {

		log.debug("[getListCluster] param = {}", region);

		CloudAuthArg authArg = new CloudAuthArg(headers.getFirst("access_key"), headers.getFirst("access_secret"));

		List<GetList> result = ackInterfaceService.getListCluster(authArg, region);

		log.debug("[getListCluster] return = {}", result.toString());

		return result;

	}


}
