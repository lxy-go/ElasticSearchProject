package com.wdjr.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ElasticSearch的配置
 */
@Configuration
public class ElasticSearchConfig {
    @Bean
    public TransportClient esClient() throws UnknownHostException{
        Settings settings = Settings.builder()
                .put("cluster.name","mangues_es")
                .put("client.transport.sniff",true)
                .build();
        InetSocketTransportAddress master = new InetSocketTransportAddress(
                InetAddress.getByName("192.168.179.131"),9300
        );
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(master);
        return client;
    }
}
