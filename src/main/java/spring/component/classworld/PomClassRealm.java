/**
 * @author XiongJie, Date: 13-9-22
 */
package spring.component.classworld;

import spring.component.core.Component;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.strategy.Strategy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * 扩展 plexus 的缺省 class realm的对象
 */
public class PomClassRealm extends ClassRealm implements Comparable<PomClassRealm>{
    private PomClassWorld         pomWorld;
    private Component             component;
    private SortedSet<ClassRealm> dependedRealms;
    /*暂时不支持 depends parent == depends parent's all modules*/
    /*如果需要这个特性，那么也应该在组件之间建立depends关系的时候直接翻译为对parent的modules的依赖*/
    //private SortedSet<ClassRealm> moduleRealms;

    public PomClassRealm(PomClassWorld world, Component component) {
        super(world, component.getId(), getParentClassLoader(world, component));
        this.pomWorld = world;
        this.component = component;
        URL url = this.component.getJarFileURL();
        if (url != null) addURL(url);
        //TODO 应该将所有的第三方包的类由一个统一的class loader加载，管理
        //而后这个class loader面向不同的组件，有许多 representation
        //跨过各种依赖关系，直接将第三方包依赖加到本身上，加速系统启动过程中的Class Load过程
        Set<URL> libUrls = this.component.getDependedPlainURLs();
        for (URL libUrl : libUrls) {
            addURL(libUrl);
        }
    }

    // ------------------------------------------------------------
    //     扩展功能，从dependencies里面加载类，资源
    // ------------------------------------------------------------

    public Class loadClassFromDepends(String name) {
        for (ClassRealm dependedRealm : dependedRealms) {
            try {
                return dependedRealm.loadClass(name);
            } catch (ClassNotFoundException e) {
                /*continue; to try next realm*/
            }
        }
        return null;
    }

    public URL loadResourceFromDepends(String name) {
        for (ClassRealm dependedRealm : dependedRealms) {
            URL resource = dependedRealm.getResource(name);
            if( resource != null)
                return resource;
        }
        return null;
    }

    public Enumeration loadResourcesFromDepends(String name) {
        ArrayList<URL> founds = new ArrayList<URL>();
        for (ClassRealm dependedRealm : dependedRealms) {
            Enumeration<URL> resources = null;
            try {
                resources = dependedRealm.getResources(name);
            } catch (IOException e) {
                /*continue;*/
            }
            if(resources != null ){
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    founds.add(url);
                }
            }
        }
        return Collections.enumeration(founds);
    }

    // ------------------------------------------------------------
    //     对 PomClassRealm的扩展
    // ------------------------------------------------------------

    void afterConstruction(){
        customizeStrategy();
        dependRealms();
        //createModuleRealms();
    }

    protected void customizeStrategy() {
        try{
            Field strategyField = getClass().getSuperclass().getDeclaredField("strategy");
            strategyField.setAccessible(true);
            Strategy strategy = new PomStrategy(this);
            strategyField.set(this, strategy);
        }catch (Exception ex){
            throw new UnsupportedOperationException("The parent ClassRealm don't support PomClassRealm hacking by refection", ex);
        }
    }

    protected void dependRealms() {
        dependedRealms = new TreeSet<ClassRealm>();
            for(Component depended : component.getAllDependedComponents()){
                if(depended.isPlain())continue;//所有的第三方包不再提供class realm
                PomClassRealm dependedRealm = findOrCreateRealm(pomWorld, depended);
                dependedRealms.add(dependedRealm);
            }
    }

    @Override
    public int compareTo(PomClassRealm another) {
        return this.component.compareTo(another.component);
    }

    //    protected void createModuleRealms() {
//        moduleRealms = new TreeSet<ClassRealm>();
//        if(component.getModules() != null && component.getModules().isEmpty()){
//            for(Component module : component.getModules()){
//                PomClassRealm moduleRealm = findOrCreateRealm(pomWorld, module);
//                moduleRealms.add(moduleRealm);
//            }
//        }
//    }

    static ClassLoader getParentClassLoader(PomClassWorld world, Component component) {
        Component parent = component.getParent();
        if (parent == null || parent.isPlain()) {
            //采用一个隔离的Class Loader的根，而不是当前运行环境的根
            return ClassLoader.getSystemClassLoader();
        } else {
            return findOrCreateRealm(world, parent);
        }
    }

    static PomClassRealm findOrCreateRealm(PomClassWorld world, Component component){
        PomClassRealm existRealm = world.getClassRealm(component);
        if(existRealm != null )
            return existRealm;
        try {
            return world.newRealm(component);
        } catch (DuplicateRealmException e) {
            throw new IllegalStateException("I'v checked the realm/component duplication, but it occurs." +
                                                    " this error occurred by concurrent issue!");
        }
    }
}
