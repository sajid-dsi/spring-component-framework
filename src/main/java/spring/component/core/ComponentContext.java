/**
 * @author XiongJie, Date: 13-11-11
 */

package spring.component.core;

import spring.component.classworld.PomClassRealm;
import spring.component.container.ComponentLoader;
import spring.component.container.ComponentRepository;
import spring.component.container.ServiceRegistry;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.springframework.context.ApplicationContext;

/** 组件上下文 */
public interface ComponentContext {
    /**
     * 为特定组件注册特定名称的特性结果
     *
     * @param component 特定组件
     * @param name      特性名称
     * @param feature   特性结果
     */
    void registerFeature(Component component, String name, Object feature);

    Object removeFeature(Component component, String name);

    ClassRealm getClassRealm(String componentId);

    <T> T getFeature(Component component, String name);

    ClassRealm getLibraryFeature(Component component);

    ApplicationContext getApplicationFeature(Component component);

    ApplicationContext getServiceFeature(Component component);

    ServiceRegistry getRegistry();

    ComponentLoader getComponentLoader();

    ComponentRepository getComponentRepository();

    PomClassRealm getMainClassLoader();
}
