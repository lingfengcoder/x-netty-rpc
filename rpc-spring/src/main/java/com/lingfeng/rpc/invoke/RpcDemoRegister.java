package com.lingfeng.rpc.invoke;

import com.lingfeng.rpc.ann.EnableRpcClient;
import com.lingfeng.rpc.ann.RpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

@Slf4j
public class RpcDemoRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private ResourceLoader resourceLoader;
    private Environment environment;

    RpcDemoRegister() {
        log.info("RpcDemoRegister ");
    }

    static void validateFallback(final Class clazz) {
        Assert.isTrue(!clazz.isInterface(), "Fallback class must implement the interface annotated by @FeignClient");
    }

    static void validateFallbackFactory(final Class clazz) {
        Assert.isTrue(!clazz.isInterface(), "Fallback factory must produce instances of fallback classes that implement the interface annotated by @FeignClient");
    }

    static String getName(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        } else {
            String host = null;

            try {
                String url;
                if (!name.startsWith("http://") && !name.startsWith("https://")) {
                    url = "http://" + name;
                } else {
                    url = name;
                }

                host = (new URI(url)).getHost();
            } catch (URISyntaxException var3) {
            }

            Assert.state(host != null, "Service id not legal hostname (" + name + ")");
            return name;
        }
    }

    static String getUrl(String url) {
        if (StringUtils.hasText(url) && (!url.startsWith("#{") || !url.contains("}"))) {
            if (!url.contains("://")) {
                url = "http://" + url;
            }

            try {
                new URL(url);
            } catch (MalformedURLException var2) {
                throw new IllegalArgumentException(url + " is malformed", var2);
            }
        }

        return url;
    }

    static String getPath(String path) {
        if (StringUtils.hasText(path)) {
            path = path.trim();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }

        return path;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        this.registerDefaultConfiguration(metadata, registry);
        this.registerFeignClients(metadata, registry);
    }

    private void registerDefaultConfiguration(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> defaultAttrs = metadata.getAnnotationAttributes(EnableRpcClient.class.getName(), true);
        if (defaultAttrs != null && defaultAttrs.containsKey("defaultConfiguration")) {
            String name;
            if (metadata.hasEnclosingClass()) {
                name = "default." + metadata.getEnclosingClassName();
            } else {
                name = "default." + metadata.getClassName();
            }

            this.registerClientConfiguration(registry, name, defaultAttrs.get("defaultConfiguration"));
        }

    }

    public void registerFeignClients(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet();
        Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableRpcClient.class.getName());
        Class<?>[] clients = attrs == null ? null : (Class[]) ((Class[]) attrs.get("clients"));
        if (clients != null && clients.length != 0) {
            Class[] var12 = clients;
            int var14 = clients.length;

            for (int var16 = 0; var16 < var14; ++var16) {
                Class<?> clazz = var12[var16];
                candidateComponents.add(new AnnotatedGenericBeanDefinition(clazz));
            }
        } else {
            ClassPathScanningCandidateComponentProvider scanner = this.getScanner();
            scanner.setResourceLoader(this.resourceLoader);
            scanner.addIncludeFilter(new AnnotationTypeFilter(RpcClient.class));
            Set<String> basePackages = this.getBasePackages(metadata);
            Iterator<String> v8 = basePackages.iterator();

            while (v8.hasNext()) {
                String basePackage = v8.next();
                candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
            }
        }

        Iterator<BeanDefinition> var13 = candidateComponents.iterator();

        while (var13.hasNext()) {
            BeanDefinition candidateComponent = var13.next();
            if (candidateComponent instanceof AnnotatedBeanDefinition) {
                AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                String className = annotationMetadata.getClassName();
                log.info("className={}", className);
                Class<? extends AnnotationMetadata> clazz = annotationMetadata.getClass();
                log.info("clazz={}", clazz);

                Assert.isTrue(annotationMetadata.isInterface(), "@RpcClient can only be specified on an interface");
                Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(RpcClient.class.getCanonicalName());
                String name = this.getClientName(attributes);
                this.registerClientConfiguration(registry, name, attributes.get("configuration"));
                this.registerRpcClient(registry, annotationMetadata, attributes);
            }
        }

    }

    private void registerRpcClient(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        Class clazz = ClassUtils.resolveClassName(className, null);
        ConfigurableBeanFactory beanFactory = registry instanceof ConfigurableBeanFactory ? (ConfigurableBeanFactory) registry : null;
        String contextId = this.getContextId(beanFactory, attributes);
        String name = this.getName(attributes);
        RpcClientBeanFactory rpcClientBeanFactory = new RpcClientBeanFactory();
        rpcClientBeanFactory.setTargetClazz(clazz);
        //动态代理生成
        //Object proxyBean = JdkDynamicProxyUtil.proxyInvoke(clazz, new RemoteProcess());
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(clazz, () -> {
            return rpcClientBeanFactory.getObject();
            //  return JdkDynamicProxyUtil.proxyInvoke(clazz, new RemoteProcess());
        });

        definition.setAutowireMode(2);
        definition.setLazyInit(true);
        this.validate(attributes);
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setAttribute("factoryBeanObjectType", className);
        beanDefinition.setAttribute("rpcClientsRegistrarFactoryBean", rpcClientBeanFactory);
        boolean primary = (Boolean) attributes.get("primary");
        beanDefinition.setPrimary(primary);
        String[] qualifiers = this.getQualifiers(attributes);
        if (ObjectUtils.isEmpty(qualifiers)) {
            qualifiers = new String[]{contextId + "RpcClient"};
        }

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, qualifiers);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
        this.registerOptionsBeanDefinition(registry, contextId);
    }

    private void validate(Map<String, Object> attributes) {
        AnnotationAttributes annotation = AnnotationAttributes.fromMap(attributes);
        // validateFallback(annotation.getClass("fallback")); feign 的失败回调 //note 后期考虑rpc失败的回调
        // validateFallbackFactory(annotation.getClass("fallbackFactory"));// feign 的失败工厂
    }

    String getName(Map<String, Object> attributes) {
        return this.getName((ConfigurableBeanFactory) null, attributes);
    }

    String getName(ConfigurableBeanFactory beanFactory, Map<String, Object> attributes) {
        String name = (String) attributes.get("serviceId");
        if (!StringUtils.hasText(name)) {
            name = (String) attributes.get("name");
        }

        if (!StringUtils.hasText(name)) {
            name = (String) attributes.get("value");
        }

        name = this.resolve(beanFactory, name);
        return getName(name);
    }

    private String getContextId(ConfigurableBeanFactory beanFactory, Map<String, Object> attributes) {
        String contextId = (String) attributes.get("contextId");
        if (!StringUtils.hasText(contextId)) {
            return this.getName(attributes);
        } else {
            contextId = this.resolve(beanFactory, contextId);
            return getName(contextId);
        }
    }

    private String resolve(ConfigurableBeanFactory beanFactory, String value) {
        if (StringUtils.hasText(value)) {
            if (beanFactory == null) {
                return this.environment.resolvePlaceholders(value);
            } else {
                BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
                String resolved = beanFactory.resolveEmbeddedValue(value);
                return resolver == null ? resolved : String.valueOf(resolver.evaluate(resolved, new BeanExpressionContext(beanFactory, (Scope) null)));
            }
        } else {
            return value;
        }
    }

    private String getUrl(ConfigurableBeanFactory beanFactory, Map<String, Object> attributes) {
        String url = this.resolve(beanFactory, (String) attributes.get("url"));
        return getUrl(url);
    }

    private String getPath(ConfigurableBeanFactory beanFactory, Map<String, Object> attributes) {
        String path = this.resolve(beanFactory, (String) attributes.get("path"));
        return getPath(path);
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation()) {
                    isCandidate = true;
                }

                return isCandidate;
            }
        };
    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableRpcClient.class.getCanonicalName());
        Set<String> basePackages = new HashSet();
        String[] values = (String[]) attributes.get("value");
        int varLen = values.length;

        int i;
        String pkg;
        for (i = 0; i < varLen; ++i) {
            pkg = values[i];
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        values = (String[]) attributes.get("basePackages");
        varLen = values.length;

        for (i = 0; i < varLen; ++i) {
            pkg = values[i];
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        Class[] basePackageClasses = (Class[]) attributes.get("basePackageClasses");
        varLen = basePackageClasses.length;

        for (i = 0; i < varLen; ++i) {
            Class<?> clazz = basePackageClasses[i];
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }

        return basePackages;
    }

    private String getQualifier(Map<String, Object> client) {
        if (client == null) {
            return null;
        } else {
            String qualifier = (String) client.get("qualifier");
            return StringUtils.hasText(qualifier) ? qualifier : null;
        }
    }

    private String[] getQualifiers(Map<String, Object> client) {
        if (client == null) {
            return null;
        } else {
            List<String> qualifierList = new ArrayList(Arrays.asList((String[]) client.get("qualifiers")));
            qualifierList.removeIf((qualifier) -> !StringUtils.hasText((CharSequence) qualifier));
            if (qualifierList.isEmpty() && this.getQualifier(client) != null) {
                qualifierList = Collections.singletonList(this.getQualifier(client));
            }
            return !qualifierList.isEmpty() ? (String[]) ((List) qualifierList).toArray(new String[0]) : null;
        }
    }

    private String getClientName(Map<String, Object> client) {
        if (client == null) {
            return null;
        } else {
            String value = (String) client.get("contextId");
            if (!StringUtils.hasText(value)) {
                value = (String) client.get("value");
            }

            if (!StringUtils.hasText(value)) {
                value = (String) client.get("name");
            }

            if (!StringUtils.hasText(value)) {
                value = (String) client.get("serviceId");
            }
            if (StringUtils.hasText(value)) {
                return value;
            } else {
                throw new IllegalStateException("Either 'name' or 'value' must be provided in @" + RpcClient.class.getSimpleName());
            }
        }
    }

    //注册客户端对应的配置类
    private void registerClientConfiguration(BeanDefinitionRegistry registry, Object name, Object configuration) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcClientSpecification.class);
        builder.addConstructorArgValue(name);
        builder.addConstructorArgValue(configuration);
        registry.registerBeanDefinition(name + "." + RpcClientSpecification.class.getSimpleName(), builder.getBeanDefinition());
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private void registerOptionsBeanDefinition(BeanDefinitionRegistry registry, String contextId) {
        if (this.isClientRefreshEnabled()) {
            String beanName = "";// Request.Options.class.getCanonicalName() + "-" + contextId;
//            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(OptionsFactoryBean.class);
//            definitionBuilder.setScope("refresh");
//            definitionBuilder.addPropertyValue("contextId", contextId);
            //  BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(definitionBuilder.getBeanDefinition(), beanName);
            // definitionHolder = ScopedProxyUtils.createScopedProxy(definitionHolder, registry, true);
            //  BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
        }

    }

    private boolean isClientRefreshEnabled() {
        return (Boolean) this.environment.getProperty("feign.client.refresh-enabled", Boolean.class, false);
    }
}
