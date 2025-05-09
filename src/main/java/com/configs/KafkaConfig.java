//package com.configs;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.KafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
//import org.springframework.kafka.listener.ContainerProperties;
//
//@Configuration
//@EnableKafka
//public class KafkaConfig {
//
//    @Bean
//    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory(
//            ConsumerFactory<String, String> consumerFactory) {
//
//        ConcurrentMessageListenerContainerFactory<String, String> factory =
//                new ConcurrentMessageListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory);
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);  // Chế độ ack thủ công
//        return factory;
//    }
//}
