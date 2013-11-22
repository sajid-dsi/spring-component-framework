/**
 * @author XiongJie, Date: 13-11-11
 */
package spring.component.container.feature;

import spring.component.classworld.PomClassWorld;
import spring.component.core.Component;
import spring.component.core.Features;
import spring.component.core.support.DefaultComponent;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;

/** 静态特性解析 */
public class StaticFeatureResolver extends AbstractFeatureResolver{

    public StaticFeatureResolver() {
        super(100);
    }

    @Override
    public String getName() {
        return Features.STATIC_FEATURE;
    }

    @Override
    public boolean hasFeature(Component component) {
        return component.getResource().exists("META-INF/pom.xml");
    }

    @Override
    public void resolve(Component component) {
        logger.debug("Resolving {} {} feature", component, getName());
        if(component.isPlain()) return;
        ClassRealm realm = resolveContext.getClassRealm(component.getId());
        if(realm == null){
            PomClassWorld world = (PomClassWorld) resolveContext.getMainClassLoader().getWorld();
            try {
                realm = world.newRealm(component);
            } catch (DuplicateRealmException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if(component instanceof DefaultComponent){
            ((DefaultComponent)component).setClassLoader(realm);
        }
        resolveContext.registerFeature(component, getName(), realm);
    }
}
