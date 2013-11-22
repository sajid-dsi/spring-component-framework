/**
 * @author XiongJie, Date: 13-9-3
 */
package spring.component.container.support;

import spring.component.classworld.PomClassRealm;
import spring.component.classworld.PomClassWorld;
import spring.component.container.*;
import spring.component.core.Component;
import spring.component.core.FeatureResolver;
import spring.component.core.exception.DependencyNotMeetException;
import spring.component.core.exception.InvalidComponentNameException;
import spring.util.LogUtils;
import org.codehaus.plexus.classworlds.launcher.ConfigurationException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 为了让Launch静态程序可以被定制化，从原 Plexus/Launch中分离出来的对象 */
public class DefaultLaunchEnvironment implements LaunchEnvironment {
    public static final int MODE_START  = 1;
    public static final int MODE_STOP   = -1;
    public static final int MODE_RELOAD = 0;

    private Logger logger = LoggerFactory.getLogger(DefaultLaunchEnvironment.class.getName());

    private ComponentRepository repository;
    private ComponentLoader     loader;

    public DefaultLaunchEnvironment() {
        String home = System.getProperty("app.home");
        if (!StringUtils.hasText(home)) {
            home = System.getProperty("user.dir");
            System.setProperty("app.home", home);
            logger.warn("app.home is not set, use user.dir as app.home: {}", home);
        }
        String launchPort = System.getProperty("app.port");
        if (!StringUtils.hasText(launchPort)) {
            System.setProperty("app.port", "1099");
            logger.warn("app.port is not set, use 1099 as default");
        }
        repository = createComponentRepository(home);
        try {
            repository.start();
        } catch (Exception e) {
            throw new UnsupportedOperationException("Can't start the default component repository, " +
                                                    "because of: " + e.getMessage(), e);
        }
    }

    /**
     * 根据Main Resource的元信息，创建相应的Launcher对象
     *
     * @param component 主启动组件
     * @return App Launcher对象
     */
    @Override
    public AppLauncher createLauncher(Component component) throws IOException, ConfigurationException {
        AppLauncher launcher = new AppLauncher(component, this);
        //配置 class world
        PomClassRealm mainRealm = launcher.configure();
        this.loader = createLoader(mainRealm);
        return launcher;
    }

    /**
     * 在本环境下启动应用程序
     *
     * @param launcher 启动器
     * @throws Exception 错误
     */
    /*package*/ void start(Executable launcher) throws Exception {
        launcher.start();
    }

    /**
     * 停止一个远程的launcher对象
     *
     * @param launcher 远程launcher对象
     */
    /*package*/ void stop(Executable launcher) {
        launcher.exit();
    }

    /**
     * 要求远程launcher对象刷新
     *
     * @param launcher 远程launcher对象
     */
    /*package*/ void reload(Executable launcher) {
        launcher.reload();
    }

    @Override
    public void execute(AppLauncher launcher, String[] args) throws Exception {
        int mode;
        List<String> list = new ArrayList<String>(args.length);
        Collections.addAll(list, args);
        if (list.contains("--stop")) {
            mode = MODE_STOP;
        } else if (list.contains("--reload")) {
            mode = MODE_RELOAD;
        } else {
            mode = MODE_START;
        }
        String host = null;
        int port = 0;
        if (list.contains("--host")) {
            host = list.get(list.indexOf("--host") + 1);
        }
        if (list.contains("--port")) {
            port = Integer.parseInt(list.get(list.indexOf("--port") + 1));
        }
        Executable executable = configure(launcher, host, port);
        try {
            switch (mode) {
                case MODE_START:
                    start(executable);
                    break;
                case MODE_STOP:
                    stop(executable);
                    break;
                case MODE_RELOAD:
                    reload(executable);
                    break;
                default:
                    start(executable);
            }
        } catch (InvocationTargetException e) {
            ClassRealm realm = launcher.getWorld().getRealm(launcher.getMainRealmName());

            URL[] constituents = realm.getURLs();

            logger.info("---------------------------------------------------");

            for (int i = 0; i < constituents.length; i++) {
                logger.info("constituent[" + i + "]: " + constituents[i]);
            }

            logger.info("---------------------------------------------------");

            // Decode ITE (if we can)
            Throwable t = e.getTargetException();

            if (t instanceof Exception) {
                throw (Exception) t;
            }
            if (t instanceof Error) {
                throw (Error) t;
            }

            // Else just toss the ITE
            throw e;
        }
    }

    protected Executable configure(AppLauncher launcher, String host, int port) {
        //设定了通过本地端口与已经启动的进程通讯
        if (host != null) {
            return new LauncherThroughPort(host, port);
        } else if (port > 0) {
            return new LauncherThroughPort(port);
        }
        return launcher;
    }


    // ------------------------------------------------------------
    //     对 ComponentRepository 代理接口的实现
    // ------------------------------------------------------------

    @Override
    public Component resolveComponent(String strDependency)
            throws DependencyNotMeetException, InvalidComponentNameException {
        return repository.resolveComponent(strDependency);
    }


    /**
     * 自顶向下的加载组件
     *
     * @param component 被加载的组件
     */
    public void load(Component component) throws IOException {
        loader.load(component);
    }

    public void unload(Component component) {
        loader.unload(component);
    }


    @Override
    public void shutdown() {
        logger.info("Shutting down");
        if (repository != null) {
            repository.stop();
        }
    }

    protected DefaultComponentRepository createComponentRepository(String home) {
        return new DefaultComponentRepository(home);
    }

    protected ComponentLoader createLoader(PomClassRealm mainRealm) {
        DefaultComponentLoader loader = new DefaultComponentLoader(repository, (PomClassWorld) mainRealm.getWorld());
        String featureResolvers = System.getProperty("component.feature.resolvers");
        if(StringUtils.hasText(featureResolvers)){
            for(String resolverFqn: featureResolvers.split(",")){
                try{
                    logger.info(LogUtils.banner("Found extended feature resolver: " + resolverFqn));
                    Class resolverClass = Class.forName(resolverFqn, true, mainRealm);
                    FeatureResolver resolver = (FeatureResolver) resolverClass.newInstance();
                    loader.registerResolver(resolver);
                }catch (Exception ex){
                    logger.error("Can't instantiate the feature resolver: " + resolverFqn, ex);
                }
            }
        }
        return loader;
    }

}
