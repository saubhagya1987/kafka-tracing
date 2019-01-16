package com.kafka.springbootkafkaproducerexample.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.kafka.springbootkafkaproducerexample.model.User;
import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;

import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.spring.TracingProducerFactory;

/**
 * @author Saubhagya.Pradhan2 This is the Kafka Configuration Class
 */
@org.springframework.context.annotation.Configuration

public class KakfaConfiguration {

	@Autowired
	private Environment env;

	@Bean
	public Tracer tracer() {
		return new Configuration("KakfaProducer",
				new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1,
						env.getProperty("jaeger.sampler.manager.host.port")),
				new Configuration.ReporterConfiguration(false, env.getProperty("jaeger.agent.host"),
						Integer.parseInt(env.getProperty("jaeger.agent.port")), 1000, 100)).getTracer();
	}

	@Bean
	public ProducerFactory<String, User> producerFactory() {
		Map<String, Object> config = new HashMap<>();

		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		// return new DefaultKafkaProducerFactory<>(config);
		return new TracingProducerFactory<>(new DefaultKafkaProducerFactory<>(config), tracer());

	}

	@Bean
	public KafkaTemplate<String, User> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

}
