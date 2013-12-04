spring-component-framework
==========================

1. ʹ�ó���
-----------

Spring component framework ��һ������SpringFramework��Maven���������΢�ں�Java����������(δ���汾��֧��WebӦ�ã���

���ܰ����㽫Ӧ�ó����и��Ϊ������С�飨һ��Jar������һ��ģ�飩���Ҷ����Ӧ�ó�����ȫû���κ������ԡ�
����Ҫ��OSGi��������Ҫʵ��BundleContext�ӿڣ��˽�MANEFEST.MF����һ��Bundle-*����

�ڴ�֮�⣬�������Ը�������Ӧ�ó��򣬲�����Maven��֧���£��������Ӧ�ó������ڿ���̬������̬��һ���ԡ�

���Ķ����½���ʱ����������ز��ο������� [ʾ������](https://github.com/Kadvin/spring-component-example)

2. ʹ�÷�ʽ
----------

### 2.1 ��ͨӦ�ó���

��������Ҫ����һ���ֲ�ʽ���򣬰����������˺Ϳͻ����������֡�

```
              +---------+           +--------+
              | server  |           | client |
   Caller --> |  |-basis|           |  |     |
              |  |-api  |<---RMI--->|  |-api |
              +---------+           +--------+

```

�������Ϳͻ��˲����ڲ�ͬ�Ľ��̿ռ䣬�໥֮��ͨ��RMI���ʣ���ͬ����api�ж���Ľӿڡ�

�������ȿͻ��˶����һ������һ��basis���������Ϊserver�ṩ�洢/���湦�ܡ�

������(caller)���Ա���Ϊ��ϵͳ�ⲿ�����һ��ģ�⣬ͨ��RMI�����Server�ĵ��á�

���ǿ��Խ������Ϊ���¼���ģ�飺

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <packaging>pom</packaging>

    <groupId>com.myapp</groupId>
    <artifactId>spring-component-example</artifactId>
    <version>1.0.0</version>

    <name>My app</name>
    <modules>
        <module>api</module>
        <module>basis</module>
        <module>server</module>
        <module>client</module>
        <module>caller</module>
    </modules>
</project>
```

#### 1. com.myapp.api

  ����ģ��֮���API.

```java
  //all below API is in this package;
  package com.myapp.api;

  /**
   * ��������API�����ͻ��˻����ⲿ�����ߵ���
   */
  public interface ServerAPI{
    /**
     * A service export to client to register
     */
    String register(String clientId, String address);

    /**
     * Receive some job assigned by outer system
     * and the server will pick a client to perform the job, cache the result.
     */
    Object perform(String job);
  }
```

```java
  /**
   * �ͻ���API��������������
   */
  public interface ClientAPI{
    /**
     * A service export to server to be assigned with some job
     */
    Object perform(String job);
  }

```

```java
  /**
   * ������񣬱��������ڲ�ģ�����
   */
  public interface CacheService{
    boolean store(String key, Object value);
    Object pick(String key);
  }
```

API��Ŀ��Maven Pom�����ļ���������:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>spring-component-example</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>api</artifactId>
    <name>My App API</name>
</project>
```

#### 2. com.myapp.client

  �ͻ���α�����£�

```java
  package com.myapp.client;

  @org.springframework.stereotype.Component
  public class ClientImpl implements ClientAPI{
    public Object perform(String job){
      //do some real staff
      //and return the result;
    }
  }
```

�ͻ��˵�Maven Pom��������:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>spring-component-example</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>client</artifactId>
    <name>My App Client</name>

    <dependencies>
      <dependency>
        <groupId>com.myapp</groupId>
        <artifactId>api</artifactId>
        <version>${project.version}</version>
      </dependency>
    </dependencies>
</project>
```

#### 3. com.myapp.basis

  �ṩ�򵥵Ļ�����񣬱�������ģ�����

```java
  package com.myapp.basis;

  @org.springframework.stereotype.Component
  public class CacheServiceImpl implements CacheService{
    private Map<String, Object> store = new HashMap<String,Object>();
    public boolean store(String key, Object value){
      store.put(key, value);
      return true;
    }

    public Object pick(String key){
      return store.get(key);
    }
  }
```

Basisģ���Pom��������:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>spring-component-example</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>basis</artifactId>
    <name>My App Basis</name>

    <dependencies>
      <dependency>
        <groupId>com.myapp</groupId>
        <artifactId>api</artifactId>
        <version>${project.version}</version>
      </dependency>
    </dependencies>
</project>
```

#### 4. com.myapp.server

  Serverģ������API��Ŀ��ͬʱ����Basisģ�顣

```java
  package com.myapp.server;

  @org.springframework.stereotype.Component
  public class ServerImpl implements ServerAPI{
    @org.springframework.beans.factory.annotation.Autowired
    private CacheService cacheService;

    private Map<String, ClientAPI> clients = new HashMap<String, ClientAPI>();

    public String register(String clientId, String address) {
        RmiProxyFactoryBean factoryBean = new RmiProxyFactoryBean();
        factoryBean.setServiceInterface(ClientAPI.class);
        factoryBean.setServiceUrl(String.format("rmi://%s:%d/client", address, 1099));
        factoryBean.afterPropertiesSet();
        ClientAPI client = (ClientAPI) factoryBean.getObject();
        String token = UUID.randomUUID().toString();
        clients.put(token, client);
        return token;
    }

    public Object perform(String job){
        // Reused cached result first
        Object result = cacheService.pick(job);
        if( result != null )
            return result;
        // pick a client to perform the job if no cached result
        ClientAPI client = pickClient();
        if( client == null )
            throw new IllegalStateException("There is no client available to perform the job: " + job);
        result = client.perform(job);
        // store the result to reused latter
        cacheService.store(job, result);
        return result;
    }

    private ClientAPI pickClient() {
        //pick a client by random
        int max = clients.size();
        int randIndex = new Random().nextInt(max);
        return (ClientAPI) clients.values().toArray()[randIndex];
    }
  }
```

Serverģ���Pom��������:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>spring-component-example</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>server</artifactId>
    <name>My App Server</name>

    <dependencies>
      <dependency>
        <groupId>com.myapp</groupId>
        <artifactId>api</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>com.myapp</groupId>
        <artifactId>basis</artifactId>
        <version>${project.version}</version>
      </dependency>
    </dependencies>
</project>
```

#### 4. com.myapp.caller

  һ�������г���֧���û�ͨ�������е��÷�������API

```java
/** Accept test caller */
public class CLI {

    /**
     * java -Dserver.port=1097 -Dserver.address=localhost -jar path/to/com.myapp.caller-1.0.0.jar jobId
     *
     * @param args jobId(mandatory)
     */
    public static void main(String[] args) {
        if (args.length < 1)
            throw new IllegalArgumentException("You must specify a job id");
        String jobId = args[0];
        RmiProxyFactoryBean factoryBean = new RmiProxyFactoryBean();
        factoryBean.setServiceInterface(ServerAPI.class);
        factoryBean.setServiceUrl(String.format("rmi://%s:%s/server",
                                                System.getProperty("server.address", "localhost"),
                                                System.getProperty("server.port", "1097")));
        factoryBean.afterPropertiesSet();
        ServerAPI server = (ServerAPI) factoryBean.getObject();
        Object result = server.perform(jobId);
        System.out.println("Got server response: " + result);
    }
}
```

��Ӧ��POM�ļ��������£�

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>spring-component-example</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>caller</artifactId>
    <name>My App Caller</name>
    <dependencies>
        <dependency>
            <groupId>com.myapp</groupId>
            <artifactId>api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${version.springframework}</version>
        </dependency>
    </dependencies>
</project>
```

����Ŀʵ�ʽ��ᱻ�����Ϊһ��uberjar���û�����ֱ�� java -jar path/to/caller.jar job ��ʽ����

### 2.2 ���������

#### 1. ��̬���

����API��Ŀ������ʱʵ��û����������/�����κ�Java����ʵ�������������ṩһЩ�ӿ�/��̬����������������ģ��ʹ��
  ������������Ϊ **��̬** �����
  ��̬�����Ӧ�ñ������Ϊ���¸�ʽ��

```
  path/to/com.myapp.api-1.0.0.jar!
    |-META-INF
    |  |-MANIFEST.MF               # һ���Դ����������
    |  |-pom.xml                   # �ؼ��ľ�̬�����ʶ���и�pom.xml������Maven��Ŀ��Pom)
    |-com
    |  |-myapp
    |  |  |-api
    |  |  |  |-ServerAPI.class
    |  |  |  |-ClientAPI.class
    |  |  |  |-CacheService.class
```

Spring Component Framework������ʱ�������META-INF/pom.xml�ļ��Ķ��壬Ϊ��������������

#### 2. Ӧ�����

ʾ���Ŀͻ�������Ϊһ������������ʱ�������У���ͨ��RMI��¶��������������ã������ǽ�������������
����淶�涨��������Ӧ���ṩһ��application.xml��META-INFĿ¼�£���Spring Context����ЩBean���Թ���

���ǽ��䶨��Ϊ **Ӧ��** ���

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="com.myapp.client"/>

    <bean name="clientExporter" class="org.springframework.remoting.rmi.RmiServiceExporter">
      <property name="serviceInterface" value="com.myapp.api.ClientAPI"/>
      <property name="serviceName" value="client"/>
      <property name="servicePort" value="1099"/>
      <property name="service" ref="clientImpl"/>
    </bean>
</beans>  
```

Ӧ�������������������:

```
  path/to/com.myapp.client-1.0.0.jar!
    |-META-INF
    |  |-MANIFEST.MF
    |  |-pom.xml                   # ��̬�����ʶ
    |  |-application.xml           # Ӧ�������ʶ
    |-com
    |  |-myapp
    |  |  |-client
    |  |  |  |-ClientImpl.class
```

Spring Component Framework������ʱ���ظ�jarʱ�������application.xml����һ��Spring Context��������������������

#### 3. �������(���ݷ����ṩ�߽�ɫ)

Basisģ��������ʱ��Ҫ����һ��CacheServiceImplʵ�������һ���Ҫ���� **��¶** ������ģ��ʹ�á�

���ǽ�����Ϊ **����** ���������Ҫ��application.xml֮�⣬���ṩһ�� **service.xml** ���£�

```xml
<?xml version="1.0" encoding="UTF-8"?>
<service xmlns="http://www.happyonroad.net/schema/service"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.happyonroad.net/schema/service
       http://www.happyonroad.net/schema/service.xsd">
    <export>
        <role>com.myapp.api.CacheService</role>
    </export>
</service>
```

�� application.xml ���ݴ�������:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="com.myapp.basis"/>
</beans>  
```

���գ�basis��Ӧ�ñ������Ϊ���¸�ʽ:

```
  path/to/com.myapp.basis-1.0.0.jar!
    |-META-INF
    |  |-MANIFEST.MF
    |  |-pom.xml                   # ��̬�����ʶ
    |  |-application.xml           # Ӧ�������ʶ
    |  |-service.xml               # ���������ʶ
    |-com
    |  |-myapp
    |  |  |-basis
    |  |  |  |-CacheServiceImpl.class
```

Spring Component Framework�ڼ������jar��֮�󣬻�ͨ��ĳ�ֻ��ƣ����������ķ��� **��¶** ��ȥ�������������ʹ�á�

#### 4. �������(�����ʹ����)

ʾ������ķ�������Ҳ��һ�� **����** ���������������Ҫ����һ��ServerImplʵ��������Ҫ����Basis�ṩ�ķ���

Ϊ�˵���������Basis��������Ҫ��service.xml����������������

```xml
<?xml version="1.0" encoding="UTF-8"?>
<service xmlns="http://www.happyonroad.net/schema/service"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.happyonroad.net/schema/service
       http://www.happyonroad.net/schema/service.xsd">
    <import>
        <role>com.myapp.api.CacheService</role>
    </import>
</service>
```

���ڲ���application.xml��������:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="com.myapp.server"/>
</beans>  
```

�������Ҫ����������¸�ʽ:

```
  path/to/com.myapp.server-1.0.0.jar!
    |-META-INF
    |  |-MANIFEST.MF
    |  |-pom.xml                   # ��̬�����ʶ
    |  |-application.xml           # Ӧ�������ʶ
    |  |-service.xml               # ���������ʶ
    |-com
    |  |-myapp
    |  |  |-server
    |  |  |  |-ServerImpl.class
```

#### 5. �������(���)

����������һ������������Ȼ�������������ṩ�ķ���Ҳ���ܱ�¶һЩ�������������

���ǿ��Խ�����������뵼��һ������service.xml���棬��ʱ�������һ�����ʽ�ķ��������

```xml
<?xml version="1.0" encoding="UTF-8"?>
<service xmlns="http://www.happyonroad.net/schema/service"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.happyonroad.net/schema/service
       http://www.happyonroad.net/schema/service.xsd">
    <import>
        <role>com.someapp.api.ServiceA</role>
        <hint>default</hint>
    </import>
    <import>
        <role>com.someapp.api.ServiceA</role>
        <hint>myapp</hint>
        <as>myappServiceA</as>
    </import>

    <export>
        <role>com.someapp.api.ServiceB</role>
        <hint>someapp</hint>
        <ref>someAppBeanNameOrId</ref>
    </export>
</service>
```

  * ��ĳ�����������ͬһ�ӿڵķ���ʵ�����ܴ��ڶ��ʱ����������ĵ���/������ϵ����ͨ�� `hint` �ڵ��������  

  * ��ĳ������ڲ���Spring Context���������Ҫ�����Ľӿڵ�ʵ��ʵ��������ͨ���趨 `ref` �ڵ������򵼳���Ӧ��bean��Ϊ����

  * ����ķ��񣬿���ͨ���趨 `as` Ԫ����Ϊ��ȡ������������ڲ���application context���Խ�����Ϊ��ͨ��bean��ͨ��spring���õ� `@Qualifier` �����޶���

### 2.3 ��Ŀ����

#### 1. �ֹ�����

ʾ����Ӧ�ó��򷢲���Ҫ��ѭһЩԼ����淶��

����ʾ�����񷢲���Ŀ¼��: path/to/server

  1. ���и�����ʱʵ��Ҫ�õ���jar�������� com.myapp.*, ��������)��Ӧ�ñ��ŵ�libĿ¼��
  2. ���еİ��ļ�����Ҫ���ϸ�ʽ: $groupId.$artifactId-$version.jar
  3. ���ڵ������������ڲ�û�а������ǵĹ淶��Ƕ pom.xml�� ������Ҫ����pom.xml�������������� lib/poms��
  4. Spring Component Framework ��Jar��Ӧ�ñ��������� boot Ŀ¼

```
  path/to/server
    |  |-boot
    |  |  |-net.happyonroad.spring-component-framework-0.0.1.jar
    |  |-lib
    |  |  |-com.myapp.server-0.0.1.jar
    |  |  |-com.myapp.api-0.0.1.jar
    |  |  |-com.myapp.basis-0.0.1.jar
    |  |  |-org.springframework.spring-beans-3.2.4.RELEASE.jar
    |  |  |-<other depended jars>
    |  |  |-poms
    |  |  |  |-org.springframework.spring-beans-3.2.4.RELEASE.pom
    |  |  |  |-<other depended poms>
```

�������¸�ʽ����֮�����ǾͿ���ʹ����������������Ӧ�ó���

```
  cd path/to/server
  java -jar boot/net.happyonroad.spring-component-framework-0.0.1.jar com.myapp.server-1.0.0
```
���һ���������Ǹ���Spring Component Framework�������ǳ������ڣ�Ҳ���Ǵ����￪ʼ����jar����

�������㽫�ῴ�����������

```
  <TODO>
```

#### 2. �Զ�����Ӧ��

������Ҫ��ʵ������ʱ����ģ��pom�ļ��������� spring-component-framework ��������ǿ�ҽ��齫��������������Ϊ runtime
  ���⿪�����ڿ���������ֱ��ʹ��spring-component-framework�ľ�̬API��

```xml
<dependencies>
  <dependency>
    <groupId>net.happyonroad</groupId>
    <artifactId>spring-component-framework</artifactId>
    <version>0.0.1</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

���ǿ�����ʾ�������client��server������һ�����ƻ���Maven�����������������ĸ��ӹ��̣�

��Ҫ������Ϣ����ο��ò���� [��ϸ˵��](https://github.com/Kadvin/spring-component-builder)

```xml
  <build>
    <plugins>
      <plugin>
        <groupId>net.happyonroad</groupId>
        <artifactId>spring-component-builder</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <executions>
          <execution>
            <id>package-app</id>
            <phase>package</phase>
            <goals><goal>package</goal></goals>
            <configuration>
              <outputDirectory>path/to/${project.artifactId}</outputDirectory>
            </configuration>
          </execution>

          <execution>
            <id>clean-app</id>
            <phase>clean</phase>
            <goals><goal>clean</goal></goals>
            <configuration>
              <outputDirectory>path/to/${project.artifactId}</outputDirectory>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

�ò��Ĭ����maven package�׶ι�����������ʾ������(client/server)�ĸ�Ŀ¼ִ��:

```bash
mvn package
```

���ǽ��ῴ���������:

```
  path/to/server
    |  |-bin
    |  |  |-start.bat
    |  |  |-stop.bat
    |  |  |-start.sh
    |  |  |-stop.sh
    |  |-config
    |  |  |-logback.xml
    |  |-boot
    |  |  |-net.happyonroad.spring-component-framework-0.0.1.jar
    |  |-lib
    |  |  |-com.myapp.server-0.0.1.jar
    |  |  |-com.myapp.api-0.0.1.jar
    |  |  |-com.myapp.basis-0.0.1.jar
    |  |  |-org.springframework.spring-beans-3.2.4.RELEASE.jar
    |  |  |-<other depended jars>
    |  |  |-poms
    |  |  |  |-org.springframework.spring-beans-3.2.4.RELEASE.pom
    |  |  |  |-<other depended poms>
    |  |-logs
    |  |-tmp
```

```
  path/to/client
    |  |-bin
    |  |  |-start.bat
    |  |  |-stop.bat
    |  |  |-start.sh
    |  |  |-stop.sh
    |  |-config
    |  |  |-logback.xml
    |  |-boot
    |  |  |-net.happyonroad.spring-component-framework-0.0.1.jar
    |  |-lib
    |  |  |-com.myapp.client-0.0.1.jar
    |  |  |-com.myapp.api-0.0.1.jar
    |  |  |-org.springframework.spring-beans-3.2.4.RELEASE.jar
    |  |  |-<other depended jars>
    |  |  |-poms
    |  |  |  |-org.springframework.spring-beans-3.2.4.RELEASE.pom
    |  |  |  |-<other depended poms>
    |  |-logs
    |  |-tmp

```

��ʱClient��Server�Ѿ�׼��������֧����Windows��Linux�����С�

3. ����ԭ��
---------------

���˻������ʣ��Ѿ���OSGi�淶�ˣ�������಻ͬʵ�֣�Springϵ���У�����һ��Spring DM Server��Ϊʲô��Ҫ�ٷ���һ�����/�����ܵ����ӣ�

�������ߣ�SpringSource���ڿ���һ������ [spring-plugin](https://github.com/spring-projects/spring-plugin) �����ܡ�

�Ҹ����ڹ����У��������Ի���OSGi����һЩӦ�ã�����ʵ���ǹ��ڸ��ӣ�������̫��ĸ�����ߣ�Լ����������������OSGiӦ�ó����������û�ˡ�

��OSGi����̶��������ʱ������ʶ����OSGi�ĸ����ԣ���Ҫ��Դ��������ʱ�Ķ�̬�ԣ�

�ҽ�����Ҫһ���򵥣������������ܣ�����ȷ����ҽ���Ҫ����һ�������Ļ����ˣ������Կ��������У�����ػ���

��ʹ����OSGi������ͼ����������һ����ǿ��Ļ����ˣ������������Կ�/�ػ������У�����������ʱ���Լ��ĸ첲����ж��������һ���µģ�

About the spring-plugin, I have referred it when I finish this project, but I think we have different concerns, it seems to enhance the application in just one application context cross many jars, just like normal spring app does.
����Spring Plugin�����ڿ�����������֮ǰ����������˿��죬���ҷ���Spring-Plugin�뱾��Ŀ���Ų�ͬ�Ĺ�ע�㡣

That is to say, it take care about connectivity more than isolation (in my opinion).
����Ϊ��spring-plugin����ע����֮��� **������** ����Spring Component Framework����ע **������** 

below is the example application context configuration I copied from it's README.
�����Ǵ�Spring Plugin��README��copy����ʾ�����ã�

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:plugin="http://www.springframework.org/schema/plugin"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/plugin http://www.springframework.org/schema/plugin/spring-plugin.xsd">

  <import resource="classpath*:com/acme/**/plugins.xml" />

  <bean id="host" class="com.acme.HostImpl">
    <property name="plugins" ref="plugins" />
  </bean>

  <plugin:list id="plugins" class="org.acme.MyPluginInterface" />
</beans>
```

�ɴ˿ɼ������Ļ��������㣬����һ���Ƚ�ǿ��� Spring Application Context����Խ�������Jar��������������ڲ�ͨ��
һ���ı�ǩ���ö����İ���Plugin���������װ����Ҳ����Spring IOC������ԭ����λ��

��Spring Component Framework��ƶ�λ�ڣ�

 * ���������һ��jar����һ�������
 * �������Ѻã���Ҫ����̫��ĸ��
 * �������ԣ�������
 * �ڿ���̬������̬����һ��

��ʵ�ʿ��������У����ǻ��ο���Maven��ʹ�õ�Plexus IOC����������ֱ��ʹ������ײ��Classworlds��Jar֮���Class Path����

TODO: ����ļ���������