package kr.co.strato.cloud.ack.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import kr.co.strato.cloud.ack.api.service.ACKInterfaceAsyncService;
import kr.co.strato.cloud.ack.plugin.vsphere.model.CreateMessageData;
import kr.co.strato.cloud.ack.plugin.vsphere.model.DeleteMessageData;
import kr.co.strato.cloud.ack.plugin.vsphere.model.ScaleMessageData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ACKAsyncController {

	@Autowired
	private ACKInterfaceAsyncService aksInterfaceAsyncService;
	
	@ApiOperation(value = "클러스터 생성")
	@PostMapping("/api/v1/cloud/sks/async/provisioning")
	public void asyncProvisioningCluster(@RequestHeader HttpHeaders headers, @RequestBody @Valid CreateMessageData messageData) {
		
		log.debug("[asyncProvisioningCluster] param = {}", messageData.toString());
		
		aksInterfaceAsyncService.asyncProvisioningCluster(messageData);
		
	}
	
	@ApiOperation(value = "클러스터 삭제")
	@DeleteMapping("/api/v1/cloud/sks/async/delete")
	public void asyncDeleteCluster(@RequestHeader HttpHeaders headers, @RequestBody @Valid DeleteMessageData messageData) {
		
		log.debug("[asyncDeleteCluster] param = {}", messageData.toString());
		
		aksInterfaceAsyncService.asyncDeleteCluster(messageData);
		
	}
	
	@ApiOperation(value = "노드 스케일 조정")
	@PostMapping("/api/v1/cloud/sks/async/scale")
	public void asyncScaleCluster(@RequestHeader HttpHeaders headers, @RequestBody @Valid ScaleMessageData messageData) {

		log.debug("[asyncScaleCluster] param = {}", messageData.toString());
		
		aksInterfaceAsyncService.asyncScaleCluster(messageData);
		
	}
	
}
