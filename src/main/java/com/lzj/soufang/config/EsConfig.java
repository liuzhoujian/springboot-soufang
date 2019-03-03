package com.lzj.soufang.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Configuration
public class EsConfig {
    private static final String CLUSTER_NODES_SPLIT_SYMBOL = ",";
    private static final String HOST_PORT_SPLIT_SYMBOL = ":";

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;

    @Value("${spring.data.elasticsearch.cluster-nodes}")
    private String clusterNodes;

    @Bean
    public TransportClient transportClient() {
        Settings settings = Settings.builder().put("cluster.name", clusterName.trim())
                .put("client.transport.sniff", false).build();

        try {
            TransportClient transportClient = new PreBuiltTransportClient(settings);

            //获取es集群地址
            String[] clusterNodeArray = clusterNodes.trim().split(CLUSTER_NODES_SPLIT_SYMBOL);
            for (String clusterNode : clusterNodeArray) {
                String[] clusterNodeInfoArray = clusterNode.trim().split(HOST_PORT_SPLIT_SYMBOL);

                TransportAddress address = new TransportAddress(new InetSocketAddress(
                        InetAddress.getByName(clusterNodeInfoArray[0]),
                        Integer.parseInt(clusterNodeInfoArray[1])
                ));

                transportClient.addTransportAddress(address);
            }

            return transportClient;

        } catch (UnknownHostException e) {
            throw new RuntimeException("elasticsearch init fail.");
        }
    }
}
