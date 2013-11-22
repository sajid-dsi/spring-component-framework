/**
 * @author XiongJie, Date: 13-9-2
 */
package spring.component.core.support;

import spring.component.core.ComponentResource;

import java.io.*;
import java.util.jar.Manifest;

/**
 * 组件以目录形式， 未封闭状态下被运行
 */
public class ComponentFolderResource extends ComponentResource {
    protected File folder;

    public ComponentFolderResource(String folder) {
        if( folder == null ){
            throw new IllegalArgumentException("The component folder can't be null");
        }
        this.folder = new File(folder);
        if( !this.folder.exists() )
            throw new IllegalArgumentException("The component folder: " + folder + " is not exist!");
        try{
            this.manifest = new Manifest(getInputStream("META-INF/MANIFEST.MF"));
        }catch (IOException ioe){
            throw new IllegalStateException("The component folder is illegal, " +
                                                    "there should be a META-INF/MANIFEST.MF to describe the component");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 重载父类方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        this.manifest.clear();
        this.manifest = null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 内部实现方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public InputStream getInputStream(String relativePath) throws IOException {
        File file = new File(folder, relativePath);
        if( !file.exists() )                      {
            String msg = String.format("Can't find any file with relative path = `%s` in folder: `%s`",
                                       relativePath, folder.getAbsolutePath());
            throw new IllegalArgumentException(msg);
        }
        return new FileInputStream(file);
    }

    @Override
    public boolean exists(String relativePath) {
        File file = new File(folder, relativePath);
        return file.exists();
    }
}
