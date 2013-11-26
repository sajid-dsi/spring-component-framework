/**
 * @author XiongJie, Date: 13-9-22
 */
package net.happyonroad.component.classworld;

import org.codehaus.plexus.classworlds.strategy.AbstractStrategy;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * 参考 SelfFirstStrategy 实现的 类/资源加载策略
 */
public class PomStrategy extends AbstractStrategy {
    protected PomClassRealm pomRealm;

    public PomStrategy(PomClassRealm realm) {
        super(realm);
        this.pomRealm = realm;
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        Class clazz = pomRealm.loadClassFromSelf(name);

        if ( clazz == null )
        {
            clazz = pomRealm.loadClassFromDepends( name );

            if ( clazz == null )
            {
                clazz = pomRealm.loadClassFromParent( name );

                if ( clazz == null )
                {
                    throw new ClassNotFoundException( name );
                }
            }
        }

        return clazz;
    }

    @Override
    public URL getResource(String name) {
        URL resource = pomRealm.loadResourceFromDepends(name);

        if ( resource == null )
        {
            resource = pomRealm.loadResourceFromSelf( name );

            if ( resource == null )
            {
                resource = pomRealm.loadResourceFromParent( name );
            }
        }

        return resource;
    }

    @Override
    public Enumeration getResources(String name) throws IOException {
        Enumeration depends = pomRealm.loadResourcesFromDepends(name);
        Enumeration self = pomRealm.loadResourcesFromSelf( name );
        Enumeration parent = pomRealm.loadResourcesFromParent( name );

        return combineResources( depends, self, parent );
    }
}
