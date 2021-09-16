package com.nepxion.discovery.plugin.framework.decorator;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.nepxion.discovery.common.delegate.DiscoveryClientDelegate;
import com.nepxion.discovery.plugin.framework.context.PluginContextAware;
import com.nepxion.discovery.plugin.framework.listener.discovery.DiscoveryListenerExecutor;

/**
 *
 * DiscoveryClient 是 spring cloud 下的一个类，  这里通过继承实现了一些方法
 *
 *
 */
public class DiscoveryClientDecorator implements DiscoveryClient, DiscoveryClientDelegate<DiscoveryClient> {
    // private static final Logger LOG = LoggerFactory.getLogger(DiscoveryClientDecorator.class);

    private DiscoveryClient discoveryClient;
    private ConfigurableApplicationContext applicationContext;
    private ConfigurableEnvironment environment;

    /**
     *
     * 构造方法
     *
     *
     * @param discoveryClient 最开始 spring 容器中的DiscoveryClient实例
     * @param applicationContext
     */
    public DiscoveryClientDecorator(DiscoveryClient discoveryClient, ConfigurableApplicationContext applicationContext) {
        this.discoveryClient = discoveryClient;
        this.applicationContext = applicationContext;
        this.environment = applicationContext.getEnvironment();
    }

    public DiscoveryClient getDelegate() {
        return discoveryClient;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        // 根据服务 Id， 获取所有的服务实例
        List<ServiceInstance> instances = getRealInstances(serviceId);
        // 开关是否开启
        Boolean discoveryControlEnabled = PluginContextAware.isDiscoveryControlEnabled(environment);
        if (discoveryControlEnabled) {
            try {
                DiscoveryListenerExecutor discoveryListenerExecutor = applicationContext.getBean(DiscoveryListenerExecutor.class);

                /**
                 *  应该是完成了核心的逻辑，
                 *  instances 直接被传递了出去， 所以这里在内部实现上， 应该是有调用 remove 方法。
                 *
                 *
                 */
                discoveryListenerExecutor.onGetInstances(serviceId, instances);
            } catch (BeansException e) {
                // LOG.warn("Get bean for DiscoveryListenerExecutor failed, ignore to executor listener");
            }
        }

        return instances;
    }

    public List<ServiceInstance> getRealInstances(String serviceId) {
        return discoveryClient.getInstances(serviceId);
    }

    @Override
    public List<String> getServices() {
        List<String> services = getRealServices();

        Boolean discoveryControlEnabled = PluginContextAware.isDiscoveryControlEnabled(environment);
        if (discoveryControlEnabled) {
            try {
                DiscoveryListenerExecutor discoveryListenerExecutor = applicationContext.getBean(DiscoveryListenerExecutor.class);
                discoveryListenerExecutor.onGetServices(services);
            } catch (BeansException e) {
                // LOG.warn("Get bean for DiscoveryListenerExecutor failed, ignore to executor listener");
            }
        }

        return services;
    }

    public List<String> getRealServices() {
        return discoveryClient.getServices();
    }

    @Override
    public String description() {
        return discoveryClient.description();
    }

    public ConfigurableEnvironment getEnvironment() {
        return environment;
    }
}