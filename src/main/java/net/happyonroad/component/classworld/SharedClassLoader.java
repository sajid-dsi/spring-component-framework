/**
 * @author XiongJie, Date: 13-11-27
 */
package net.happyonroad.component.classworld;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/** 共享的第三方类加载器 */
class SharedClassLoader extends URLClassLoader{
    private final Set<URL> allUrls;
    public SharedClassLoader() {
        super(new URL[0]);
        allUrls = new HashSet<URL>();
    }

    public void addURLs(Set<URL> urls) {
        for (URL url : urls) {
            if(!allUrls.contains(url)){
                allUrls.add(url);
                super.addURL(url);
            }
        }
    }
}
