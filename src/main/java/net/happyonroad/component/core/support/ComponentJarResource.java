/**
 * @author XiongJie, Date: 13-8-28
 */
package net.happyonroad.component.core.support;

import net.happyonroad.component.core.ComponentResource;
import net.happyonroad.component.core.exception.ResourceNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 组件以Jar包形式，封闭状态运行
 */
public class ComponentJarResource extends ComponentResource {

    protected JarFile file;

    public ComponentJarResource(File file) {
        try {
            this.file = new JarFile(file);
            this.manifest = this.file.getManifest();
        } catch (IOException e) {
            throw new IllegalArgumentException("Bad component jar file: " + file.getPath(), e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 额外扩展的对外方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public File getFile() {
        return new File(file.getName());
    }

    public void close(){
        this.manifest.clear();
        this.manifest = null;
        try{ this.file.close(); } catch (IOException ex){ /**/ }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 重载父类方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 内部实现方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public InputStream getInputStream(String innerPath) throws IOException {
        JarEntry entry = file.getJarEntry(innerPath);
        if( entry == null )
            throw new ResourceNotFoundException("Can't find " + innerPath + " from " + file.getName());
        return file.getInputStream(entry);
    }

    @Override
    public boolean exists(String relativePath) {
        return null != file.getJarEntry(relativePath);
    }
}
