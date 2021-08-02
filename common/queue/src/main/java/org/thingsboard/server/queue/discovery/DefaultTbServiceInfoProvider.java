/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.queue.discovery;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.msg.queue.ServiceType;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.gen.transport.TransportProtos.ServiceInfo;
import org.thingsboard.server.queue.settings.TbQueueRuleEngineSettings;
import org.thingsboard.server.queue.settings.TbRuleEngineQueueConfiguration;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 通过ServiceInfo类来加载thingsboard.yml中的RuleEngineQueueConfiguration信息。
 * ruleEngineSettings包含topic是tb_rule_engine，queue队列有三个分别是:
 * 1. name: Main topic: tb_rule_engine.main partition: 10
 * 2. name: HighPriority topic: tb_rule_engine.hp partition: 10
 * 3. name: SequentialByOriginator topic: tb_rule_engine.sq partition: 10
 */
@Component
@Slf4j
public class DefaultTbServiceInfoProvider implements TbServiceInfoProvider {

    @Getter
    @Value("${service.id:#{null}}")
    private String serviceId;

    @Getter
    @Value("${service.type:monolith}")
    private String serviceType;

    @Getter
    @Value("${service.tenant_id:}")
    private String tenantIdStr;

    @Autowired(required = false)
    private TbQueueRuleEngineSettings ruleEngineSettings;

    private List<ServiceType> serviceTypes;
    private ServiceInfo serviceInfo;
    private TenantId isolatedTenant;

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(serviceId)) {
            try {
                //获取本机的HostName作为serviceId
                serviceId = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                serviceId = org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(10);
            }
        }
        log.info("Current Service ID: {}", serviceId);
        //serviceType是配置文件下service.type的值，默认为monolith
        //serviceTypes将会是一个List包含TB_CORE, TB_RULE_ENGINE, TB_TRANSPORT, JS_EXECUTOR ①
        if (serviceType.equalsIgnoreCase("monolith")) {
            serviceTypes = Collections.unmodifiableList(Arrays.asList(ServiceType.values()));
        } else {
            serviceTypes = Collections.singletonList(ServiceType.of(serviceType));
        }
        ServiceInfo.Builder builder = ServiceInfo.newBuilder()
                .setServiceId(serviceId)
                .addAllServiceTypes(serviceTypes.stream().map(ServiceType::name).collect(Collectors.toList()));
        UUID tenantId;
        //tenantIdStr是配置文件中service.tenant_id的值，默认情况下为空，isolatedTenant也就为空了
        if (!StringUtils.isEmpty(tenantIdStr)) {
            tenantId = UUID.fromString(tenantIdStr);
            isolatedTenant = new TenantId(tenantId);
        } else {
            tenantId = TenantId.NULL_UUID;
        }
        //返回此 uuid 的 128 位值中的最高有效 64 位和最低64位
        builder.setTenantIdMSB(tenantId.getMostSignificantBits());
        builder.setTenantIdLSB(tenantId.getLeastSignificantBits());

        //ruleEngineSettings是一个TbQueueRuleEngineSettings的一个实例，读取queue.rule-engine下的值
        //ruleEngineSettings包含topic是tb_rule_engine，queue队列有三个分别是:
        // 1. name: Main topic: tb_rule_engine.main partition: 10
        // 2. name: HighPriority topic: tb_rule_engine.hp partition: 10
        // 3. name: SequentialByOriginator topic: tb_rule_engine.sq partition: 10
        if (serviceTypes.contains(ServiceType.TB_RULE_ENGINE) && ruleEngineSettings != null) {
            for (TbRuleEngineQueueConfiguration queue : ruleEngineSettings.getQueues()) {
                TransportProtos.QueueInfo queueInfo = TransportProtos.QueueInfo.newBuilder()
                        .setName(queue.getName())
                        .setTopic(queue.getTopic())
                        .setPartitions(queue.getPartitions()).build();
                builder.addRuleEngineQueues(queueInfo);
            }
        }

        serviceInfo = builder.build();
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    @Override
    public boolean isService(ServiceType serviceType) {
        return serviceTypes.contains(serviceType);
    }

    @Override
    public Optional<TenantId> getIsolatedTenant() {
        return Optional.ofNullable(isolatedTenant);
    }
}
