package com.user.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

    @Autowired
    private EurekaInstanceConfigBean eurekaInstanceConfig;

    @GetMapping("/workId")
    public String getWorkId() {
        String instanceId = eurekaInstanceConfig.getInstanceId();

        System.out.println(instanceId);
        // 从 instanceId 中提取 workId
        String workId = extractWorkId(instanceId);
        return eurekaInstanceConfig.getMetadataMap().get("datacenter");
    }

    private String extractWorkId(String instanceId) {
        // 根据实际的 instanceId 格式提取 workId
        // 这里给出一个示例，假设 instanceId 的格式为 "appName@hostname:randomId"
        String[] parts = instanceId.split("@");
        if (parts.length == 2) {
            return parts[1].split(":")[0];
        }
        return null;
    }
}

