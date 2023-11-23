package kr.co.strato.cloud.ack.plugin.common.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaConsumerService {

	@Autowired
	private ClusterJobService clusterService;
	
	@KafkaListener(
			topics 				= "${plugin.kafka.topic.request}", 
			groupId 			= "${plugin.kafka.group}", 
			containerFactory 	= "kafkaListenerContainerFactory")
    public void vsphereConsumerMessage(String message) throws IOException {
    	
		log.info("Listen Sample Consumer >>> {}", message);
        clusterService.consumerMessage(message);
	}
	
}
