/**
 * @author XiongJie, Date: 13-10-29
 */
package net.happyonroad.component.container.support;

import net.happyonroad.component.classworld.PomClassRealm;
import net.happyonroad.component.classworld.PomClassWorld;
import net.happyonroad.component.core.Component;
import net.happyonroad.util.TempFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.junit.*;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;

/** 组件加载器的测试 */
public class DefaultComponentLoaderTest {

    private static Project project = new Project();
    private static File tempFolder;

    private DefaultComponentLoader     loader;
    private DefaultComponentRepository repository;
    private PomClassWorld              world;
    private Component                  target;

    @BeforeClass
    public static void setUpTotal() throws Exception {
        tempFolder = TempFile.tempFolder();

        createPom("comp_0", "spring.test-0.0.1", tempFolder);
        if("create".equals(System.getProperty("spring.test.action", "copy") )){
            createJar("comp_1", "spring.test.comp_1-0.0.1", "spring/test/api", tempFolder);
            createJar("comp_2", "spring.test.comp_2-0.0.1", "spring/test/standalone", tempFolder);
            createJar("comp_3", "spring.test.comp_3-0.0.1", "spring/test/provider", tempFolder);
            createJar("comp_4", "spring.test.comp_4-0.0.1", "spring/test/user", tempFolder);
            createJar("comp_5", "spring.test.comp_5-0.0.1", "spring/test/mixed", tempFolder);
            createJar("comp_6", "spring.test.comp_6-0.0.1", "spring/test/scan", tempFolder, new Filter("Provider"));
            createJar("comp_7", "spring.test.comp_7-0.0.1", "spring/test/scan", tempFolder, new Filter("User"));
        }else{
            copyJar("spring.test.comp_1-0.0.1", tempFolder);
            copyJar("spring.test.comp_2-0.0.1", tempFolder);
            copyJar("spring.test.comp_3-0.0.1", tempFolder);
            copyJar("spring.test.comp_4-0.0.1", tempFolder);
            copyJar("spring.test.comp_5-0.0.1", tempFolder);
            copyJar("spring.test.comp_6-0.0.1", tempFolder);
            copyJar("spring.test.comp_7-0.0.1", tempFolder);
        }

    }

    @AfterClass
    public static void tearDownTotal() throws Exception {
        try {
            FileUtils.deleteDirectory(tempFolder);
        } catch (IOException e) {
            e.printStackTrace();//TODO resolve it later
        }
    }

    @Before
    public void setUp() throws Exception {
        repository = new DefaultComponentRepository(tempFolder.getPath());
        world = new PomClassWorld();
        loader = new DefaultComponentLoader(repository, world);
        repository.start();
    }

    @After
    public void tearDown() throws Exception {
        try {
            loader.unload(target);
        }catch (Exception ex){
            System.err.println(ex.getMessage());
        } finally {
            repository.stop();
        }
    }

    /**
     * 测试目的：
     * 测试加载一个静态组件
     * <p>验证方式：
     * <ul>
     * <li>静态组件被标记为已加载
     * <li>能够通过其Realm获取其jar包相应的资源
     * <li>能够通过其Realm加载其中特有的类
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testLoadLibrary() throws Exception {
        target = repository.resolveComponent("spring.test.comp_1-0.0.1");
        PomClassRealm realm = world.newRealm(target);
        loader.load(target);
        Assert.assertTrue(loader.isLoaded(target));
        URL url = realm.getResource("spring/test/api/ServiceUser.class");
        Assert.assertNotNull(url);
        Class spClass = realm.loadClass("spring.test.api.ServiceProvider");
        Assert.assertNotNull(spClass);
    }

    /**
     * 测试目的：
     * 测试加载一个应用组件
     * <p>验证方式：
     * <ul>
     * <li>应用组件被标记为已加载
     * <li>能够通过其Realm获取其jar包相应的资源
     * <li>能够通过其Realm加载其中特有的类
     * <li>能够获取到其加载之后的Application Context
     * <li>能够从该Application Context获取到正确组装的对象
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testLoadApplication() throws Exception {
        target = repository.resolveComponent("spring.test.comp_2-0.0.1");
        PomClassRealm realm = world.newRealm(target);
        loader.load(target);
        Assert.assertTrue(loader.isLoaded(target));
        URL url = realm.getResource("spring/test/standalone/StandaloneUser.class");
        Assert.assertNotNull(url);
        Class suClass = realm.loadClass("spring.test.api.ServiceUser");
        Class spClass = realm.loadClass("spring.test.standalone.StandaloneProvider");
        Assert.assertNotNull(spClass);
        ApplicationContext context = loader.getApplicationFeature(target);
        Assert.assertNotNull(context);
        /*这个地方不能用 literal 方式加载的类，因为其与Loader里面的class不是同一个Loader*/
        /*如果解析的时候，没有做特殊的realm限制，组件解析的对象将会是当前classpath下的，而不是组件内部的*/
        /*反而致使下面的测试，literal方式可以通过，suClass方式不能通过*/
        /*所以，我采用的方式是将这些测试类放到额外的非classpath，在resource里面存放其jars*/
        Object serviceUser = context.getBean(suClass);
        Assert.assertNotNull(serviceUser);
        Assert.assertEquals("[ StandaloneUser ] message by standalone", invokeWork(serviceUser));
    }

    /**
     * 测试目的：
     * 测试加载一个提供服务的服务组件
     * <p>验证方式：
     * <ul>
     * <li>服务组件被标记为已加载
     * <li>能够通过其Realm获取其jar包相应的资源
     * <li>能够通过其Realm加载其中特有的类
     * <li>能够获取到其加载之后的Application Context
     * <li>能够从该Application Context获取到正确组装的对象
     * <li>在系统的服务注册表里面，有对应被Export的对象</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testLoadServiceWithExport() throws Exception {
        target = repository.resolveComponent("spring.test.comp_3-0.0.1");
        PomClassRealm realm = world.newRealm(target);
        loader.load(target);
        Assert.assertTrue(loader.isLoaded(target));
        URL url = realm.getResource("spring/test/provider/TestServiceProvider.class");
        Assert.assertNotNull(url);
        Class spClass = realm.loadClass("spring.test.api.ServiceProvider");
        Class tspClass = realm.loadClass("spring.test.provider.TestServiceProvider");
        Assert.assertNotNull(tspClass);
        ApplicationContext serviceContext = loader.getServiceFeature(target);
        Assert.assertNotNull(serviceContext);
        ApplicationContext context = loader.getApplicationFeature(target);
        Assert.assertNotNull(context);
        Object serviceProvider = context.getBean(spClass);
        Assert.assertNotNull(serviceProvider);
        Assert.assertEquals("[ Test ] message by test", invokeProvide(serviceProvider, "Test"));

        Assert.assertEquals(serviceProvider, loader.registry.getService(spClass));
    }

    /**
     * 测试目的：
     * 测试加载一个引用其他服务的服务组件
     * <p>验证方式：
     * <ul>
     * <li>服务组件被标记为已加载
     * <li>能够通过其Realm获取其jar包相应的资源
     * <li>能够通过其Realm加载其中特有的类
     * <li>能够获取到其加载之后的Application Context
     * <li>能够从该Application Context获取到正确组装的对象
     * <li>引用的服务存在</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testLoadServiceWithImport() throws Exception {
        target = repository.resolveComponent("spring.test.comp_4-0.0.1");
        PomClassRealm realm = world.newRealm(target);
        loader.load(target);
        Assert.assertTrue(loader.isLoaded(target));
        URL url = realm.getResource("spring/test/provider/TestServiceProvider.class");
        Assert.assertNotNull(url);
        Class suClass = realm.loadClass("spring.test.api.ServiceUser");
        Class tsuClass = realm.loadClass("spring.test.user.TestServiceUser");
        Assert.assertNotNull(tsuClass);
        ApplicationContext serviceContext = loader.getServiceFeature(target);
        Assert.assertNotNull(serviceContext);
        ApplicationContext context = loader.getApplicationFeature(target);
        Assert.assertNotNull(context);
        Object serviceUser = context.getBean(suClass);
        Assert.assertNotNull(serviceUser);
        Assert.assertEquals("[ TestServiceUser ] message by test", invokeWork(serviceUser));
    }
    /**
     * 测试目的：
     * 测试加载一个既暴露服务，又引用其他服务的服务组件
     * <p>验证方式：
     * <ul>
     * <li>服务组件被标记为已加载
     * <li>能够通过其Realm获取其jar包相应的资源
     * <li>能够通过其Realm加载其中特有的类
     * <li>能够获取到其加载之后的Application Context
     * <li>能够从该Application Context获取到正确组装的对象
     * <li>暴露的服务在注册表中有对应表项</li>
     * <li>引用的服务存在</li>
     * <li>多个引用均可以获得</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testLoadServiceWithExportAndImport() throws Exception {
        target = repository.resolveComponent("spring.test.comp_5-0.0.1");
        PomClassRealm realm = world.newRealm(target);
        loader.load(target);
        Assert.assertTrue(loader.isLoaded(target));
        URL url = realm.getResource("spring/test/mixed/MixedServiceProvider.class");
        Assert.assertNotNull(url);
        Class suClass = realm.loadClass("spring.test.api.ServiceUser");
        Class msuClass = realm.loadClass("spring.test.mixed.MixedServiceUser");
        Assert.assertNotNull(msuClass);
        ApplicationContext serviceContext = loader.getServiceFeature(target);
        Assert.assertNotNull(serviceContext);
        ApplicationContext context = loader.getApplicationFeature(target);
        Assert.assertNotNull(context);
        Object serviceUser = context.getBean(suClass);
        Assert.assertNotNull(serviceUser);
        String message = (String) invokeWork(serviceUser);
        Assert.assertTrue(message.contains("[ MixedServiceUser ] message by test"));
        Assert.assertTrue(message.contains("[ MixedServiceUser ] message by mixed"));
    }

    /**
     * <dl>
     * <dt>Purpose:</dt>
     * <dd>Spring components scanner(context:component-scan) behavior </dd>
     * <dt>Assumption:</dt>
     * <dd>There are two service components:</dd>
     * <dd>component-1: spring.test.scan.Provider (exported service)</dd>
     * <dd>component-2: spring.test.scan.User (depends service: provider)</dd>
     * <dt>Verification:</dt>
     * <dd>component-2 scanner won't instantiate the component-1's provider</dd>
     * <dd>component-2 scanner depends on component-1's provider by service</dd>
     * </dl>
     *
     * @throws Exception Any Exception
     */
    @Test
    @Ignore("Todo fix this case")
    public void testScanComponents() throws Exception {
        target = repository.resolveComponent("spring.test.comp_7-0.0.1");
        PomClassRealm realm = world.newRealm(target);
        loader.load(target);
        Assert.assertTrue(loader.isLoaded(target));
        ApplicationContext context = loader.getApplicationFeature(target);
        Assert.assertNotNull(context);
        Class suClass = realm.loadClass("spring.test.scan.User");
        Object user = context.getBean(suClass);
        String message = (String) invokeWork(user);
        Assert.assertEquals("Work by provider: 1/1", message);
    }

    private static void createPom(String folder, String compName, File root) throws IOException {
        String path = folder + "/pom.xml";
        URL source = DefaultComponentLoaderTest.class.getClassLoader().getResource(path);
        File destination = new File(root, "lib/poms/" + compName + ".pom");
        FileUtils.copyURLToFile(source, destination);
    }

    private static void copyJar(String name, File tempFolder)throws IOException{
        URL jar = DefaultComponentLoaderTest.class.getClassLoader().getResource("jars/"+ name + ".jar");
        File file = new File(tempFolder, "lib/" + name + ".jar");
        FileUtils.copyURLToFile(jar, file );
    }

    private static void createJar(String folder, String compName, String classPath, File root) throws IOException {
        createJar(folder, compName, classPath, root, null);
    }

    private static void createJar(String folder, String compName, String classPath, File root, IOFileFilter filter) throws IOException {
        URL metaInf = DefaultComponentLoaderTest.class.getClassLoader().getResource(folder);
        URL classes = DefaultComponentLoaderTest.class.getClassLoader().getResource(classPath);
        assert metaInf != null;
        assert classes != null;
        File destination = new File(root, "temp/" + compName);
        FileUtils.copyDirectory(new File(metaInf.getPath()), destination);
        //FileUtils.copyDirectory(new File(classes.getPath()), new File(destination, classPath));
        Collection<File> classesFiles = FileUtils.listFiles(new File(classes.getPath()), new String[]{"class"}, true);
        for (File classesFile : classesFiles) {
            if( filter != null && !filter.accept(classesFile))continue;
            FileUtils.copyFileToDirectory(classesFile, new File(destination, classPath ));
        }
        File file = new File(root, "lib/" + compName + ".jar");
        Jar jar = new Jar();
        jar.setProject(project);
        jar.setDestFile(file);
        jar.setBasedir(destination);
        jar.execute();
    }

    private static Object invokeWork(Object target){
        try{
            Method method = target.getClass().getMethod("work");
            return method.invoke(target);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    private static Object invokeProvide(Object target, String msg){
        try{
            Method method = target.getClass().getMethod("provide", String.class);
            return method.invoke(target, msg);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    private static class Filter implements IOFileFilter{
        private String prefix;

        private Filter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean accept(File file) {
            return file.getName().startsWith(prefix);
        }

        @Override
        public boolean accept(File dir, String name) {
            return true;
        }
    }
}