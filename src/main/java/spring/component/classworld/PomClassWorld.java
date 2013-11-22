/**
 * @author XiongJie, Date: 13-8-29
 */
package spring.component.classworld;

import spring.component.core.Component;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.ClassWorldListener;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 根据Pom创建一个Classworld，其中的Realm为其他依赖的Jar的Pom
 */
public class PomClassWorld extends ClassWorld{
    private Logger logger = LoggerFactory.getLogger(PomClassWorld.class.getName());
    Map<String, PomClassRealm> stolenRealms;
    List<ClassWorldListener> stolenListeners;
    String mainComponentId;

    public PomClassWorld() {
        try {
            Field realmField = getClass().getSuperclass().getDeclaredField("realms");
            realmField.setAccessible(true);
            //noinspection unchecked
            stolenRealms = (Map<String, PomClassRealm>) realmField.get(this);
            Field listenersField = getClass().getSuperclass().getDeclaredField("listeners");
            listenersField.setAccessible(true);
            //noinspection unchecked
            stolenListeners = (List<ClassWorldListener>) listenersField.get(this);
        }catch (Exception ex){
            throw new UnsupportedOperationException("The parent ClassWorld do not support PomClassWorld hacking by refection", ex);
        }
    }

    public PomClassRealm newRealm(Component component) throws DuplicateRealmException {
        String id = component.getId();
        if ( getClassRealm( id ) != null)
        {
            throw new DuplicateRealmException( this, id );
        }

        logger.debug("Creating realm for {}", component);
        PomClassRealm realm = new PomClassRealm( this, component );
        stolenRealms.put(id, realm);

        realm.afterConstruction();

        for (ClassWorldListener listener : stolenListeners) {
            listener.realmCreated(realm);
        }
        logger.debug("Created  realm for {}", component);
        return realm;
    }

    @Override
    public synchronized PomClassRealm getClassRealm(String id) {
        return (PomClassRealm) super.getClassRealm(id);
    }

    public synchronized PomClassRealm getClassRealm(Component component) {
        return (PomClassRealm) super.getClassRealm(component.getId());
    }

    public PomClassRealm getRealm(Component component) throws NoSuchRealmException {
        return (PomClassRealm) getRealm(component.getId());
    }

    public void setMainComponentId(String mainComponentId) {
        this.mainComponentId = mainComponentId;
    }

    public PomClassRealm getMainRealm(){
        return getClassRealm(this.mainComponentId);
    }
}
