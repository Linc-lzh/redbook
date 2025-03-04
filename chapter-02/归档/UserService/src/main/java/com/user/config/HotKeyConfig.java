package com.user.config;

import com.jd.platform.hotkey.client.ClientStarter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class HotKeyConfig {

    @Value("${hotkey.app-name}")
    private String appName;

    @Value("${hotkey.etcd-server}")
    private String etcdServer;

    @Value("${hotkey.caffeine-size}")
    private int caffeineSize;

    @PostConstruct
    public void initHotkey() {
        ClientStarter.Builder builder = new ClientStarter.Builder();
        ClientStarter starter = builder
                .setAppName(appName)
                .setEtcdServer(etcdServer)
                .setCaffeineSize(caffeineSize)
                .build();
        starter.startPipeline();
    }
}

