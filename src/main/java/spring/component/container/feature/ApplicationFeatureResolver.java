/**
 * @author XiongJie, Date: 13-11-11
 */
package spring.component.container.feature;

import spring.component.container.support.ComponentInputStreamResource;
import spring.component.core.Component;
import spring.component.core.ComponentResource;
import spring.component.core.Features;
import spring.component.core.exception.ResourceNotFoundException;
import spring.integrate.ComponentApplicationContext;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;

/** 加载应用组件 */
public class ApplicationFeatureResolver extends SpringFeatureResolver {

    public ApplicationFeatureResolver() {
        super(80);
    }

    @Override
    public String getName() {
        return Features.APPLICATION_FEATURE;
    }

    @Override
    public boolean hasFeature(Component component) {
        ComponentResource resource = component.getResource();
        return resource.exists("META-INF/application.xml");
    }

    @Override
    public void resolve(Component component) throws IOException {
        logger.debug("Resolving {} {} feature", component, getName());
        ComponentResource resource = component.getResource();
        InputStream applicationStream = resource.getApplicationStream();
        try {
            ClassRealm realm = resolveContext.getClassRealm(component.getId());
            ApplicationContext parent = resolveContext.getServiceFeature(component);
            if(parent == null )
                parent = combineDependedApplicationAsParentContext(component);
            AbstractApplicationContext context = resolveApplicationContext(component, realm, applicationStream, parent);
            context.start();
            resolveContext.registerFeature(component, getName(), context);
        } finally {
            applicationStream.close();
        }
    }

    /**
     * 解析独立的应用程序包
     *
     * @param component         组件对象
     * @param realm             类环境
     * @param applicationStream application.xml的文件流
     * @return 加载出来的应用程序上下文
     */
    protected AbstractApplicationContext resolveApplicationContext(Component component,
                                                                   ClassRealm realm,
                                                                   InputStream applicationStream,
                                                                   ApplicationContext parent) {
        ComponentApplicationContext context = new ComponentApplicationContext(component, realm, parent);
        InputStreamResource resource = new ComponentInputStreamResource(component,
                                                                        applicationStream,
                                                                        "META-INF/application.xml");
        try {
            context.load(resource);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Can't load context from application stream", e);
        }
        if (parent == null) {
            //如果不存在父上下文，就把这些对服务组件的辅助对象，直接注册在这个上下文中
            registerServiceHelpers(component, context, realm);
        }
        context.refresh();
        return context;
    }

}
