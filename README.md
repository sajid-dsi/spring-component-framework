spring-component-framework
==========================

1. Scenario
-----------

The spring component framework is used to setup a standalone application which is based on SpringFramework.

It can help you decouple your application into several components cleanly.

2. Usage
----------

### 2.1 Normal application

Given you have a distributed application which contains two parts: server and client, 
and your have separated this project into several modules:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <packaging>pom</packaging>

    <groupId>com.myapp</groupId>
    <artifactId>root</artifactId>
    <version>0.0.1</version>

    <name>My app</name>
    <modules>
        <module>api</module>
        <module>basic</module>
        <module>server</module>
        <module>client</module>
    </modules>
</project>
```

1. com.myapp.api

  Define the api between the server and client

```java
  //all below API is in this package;
  package com.myapp.api;

  /**
   * The Server API, used by client
   */
  public interface ServerAPI{
    /**
     * A service export to client to register
     */
    String register(String clientId, String address);

    /**
     * Receive some job assigned by outer system
     * and the server should pick a client to perform the job really
     */
    Object perform(String job);
  }

  /**
   * The Client API, used by server
   */
  public interface ClientAPI{
    /**
     * A service export to server to be assigned with some job
     */
    Object perform(String job);
  }

  /**
   * A shared service, which will be used by server and client both
   */
  public interface CacheService{
    boolean store(String key, Object value);
    Object pick(String key);
  }
```

and the pom of api:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>root</artifactId>
        <version>0.0.1</version>
    </parent>
    <artifactId>api</artifactId>
    <name>My App API</name>
</project>
```

2. com.myapp.client

  The client runtime part.

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

and the pom of client looks like:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>root</artifactId>
        <version>0.0.1</version>
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

3. com.myapp.basis

  Provide some basic services which can be deployed and used by server.

```java
  package com.myapp.basis;

  @org.springframework.stereotype.Component
  public class CacheServiceImpl implements CacheService{
    private Map<String, Object> store = new HashMap<String,Object>();
    public boolean store(String key, Object value){
      store.put(key, value);
      reture true;
    }

    public Object pick(String key){
      return store.get(key);
    }
  }
```

and the pom of the basis:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>root</artifactId>
        <version>0.0.1</version>
    </parent>
    <artifactId>basis</artifactId>
    <name>My App Basis</name>
</project>
```

4. com.myapp.server

  The server runtime part.

```java
  package com.myapp.server;

  @org.springframework.stereotype.Component
  public class ServerImpl implements ServerAPI{
    @org.springframework.stereotype.Autowired
    private CacheService cacheService;

    private List<ClientAPI> clients = new LinkedList<ClientAPI>();

    public void register(String clientId, String address){
      // the RemoteClientProxy is a proxy bean refer to the client at specific address by RMI or other technology
      ClientAPI client = new RemoteClientProxy(clientId, address);
      clients.add(client);
    }

    public Object perform(String job){
      // Reused cached result first
      String result = cacheService.get(job);
      if( result != null )
        return result;
      // pick a client to perform the job if no cached result
      ClientAPI client = pick();
      if( client == null ) 
        throw new IllegalStateException("There is no client available to perform the job: " + job);
      result = client.perform(job);
      // store the result to reused latter
      cacheService.store(job, result);
      return result;
    }
  }
```

and the pom of server:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.myapp</groupId>
        <artifactId>root</artifactId>
        <version>0.0.1</version>
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

### 2.2 Componentization

1. Because of the api does not provide any bean instances in runtime, we treat it as a static component.

  you just need package the this project as:

```
  path/to/com.myapp.api-1.0.0.jar!
    |-META-INF
    |  |-MANIFEST.MF               # Normal, generated by package tool
    |  |-pom.xml                   # just the pom of api projects
    |-com
    |  |-myapp
    |  |  |-api
    |  |  |  |-ServerAPI.class
    |  |  |  |-ClientAPI.class
    |  |  |  |-CacheService.class
```

2. Because of the client runs in a standalone runtime, and it export services by RMI instead of direct bean,
you should use spring application context to manage them
and we treat it as an application component.

  So create an application.xml as:

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

    <bean name="cacheServiceExporter" class="org.springframework.remoting.rmi.RmiServiceExporter">
      <property name="serviceInterface" value="com.myapp.ClientAPI"/>
      <property name="serviceName" value="clientImpl"/>
      <property name="servicePort" value="1098"/>
      <property name="service" ref="importedCacheService"/>
    </bean>
</beans>  
```

and package this project:

```
  path/to/com.myapp.client-1.0.0.jar!
    |-META-INF
    |  |-MANIFEST.MF
    |  |-pom.xml                   # just the pom of basis projects
    |  |-application.xml           # spring context defined above
    |-com
    |  |-myapp
    |  |  |-client
    |  |  |  |-ClientImpl.class
```

3. Because of the basis need create a CacheServiceImpl bean in runtime and export it as a shared service,

 We treat it as a service component which contains a service.xml besides application.xml:

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

and it's an application component also:

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

at last, package the client as:

```
  path/to/com.myapp.client-1.0.0.jar!
    |-META-INF
    |  |-MANIFEST.MF
    |  |-pom.xml                   # just the pom of basis projects
    |  |-application.xml           # spring context defined above
    |  |-service.xml               # service declaration 
    |-com
    |  |-myapp
    |  |  |-client
    |  |  |  |-ClientImpl.class
```


4. The server is a service component, which will create some beans not only, depends some other services but also in runtime.

Declair imported service:

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

Organize inner beans by:

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

at last, package the server as:

```
  path/to/com.myapp.server-1.0.0.jar!
    |-META-INF
    |  |-MANIFEST.MF
    |  |-pom.xml                   # just the pom of basis projects
    |  |-application.xml           # spring context defined above
    |  |-service.xml               # service declaration 
    |-com
    |  |-myapp
    |  |  |-server
    |  |  |  |-ServerImpl.class
```

### 2.3 Deploy/automation the project

You can add spring-component-framework as runtime dependency to the real runtime project's pom(client and server)

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

and you need add a customized plugin to package the client/server app:

```xml
  <build>
    <plugins>
      <plugin>
        <groupId>net.happyonroad</groupId>
        <artifactId>spring-component-builder</artifactId>
        <version>0.0.1</version>
        <executions>
          <execution>
            <id>package-app</id>
            <phase>package</phase>
            <goals><goal>run</goal></goals>
            <configuration>
              <outputDirectory>path/to/${project.artifactId}</outputDirectory>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

when you execute such commands in the project root:

```bash
mvn package
```

you should saw your app is build like below:

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
    |  |  |-org.springframework.spring-beans-3.2.14.RELEASE.jar
    |  |  |-<other depended jars>
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
    |  |  |-org.springframework.spring-beans-3.2.14.RELEASE.jar
    |  |  |-<other depended jars>
    |  |-logs
    |  |-tmp

```

and your client and server is ready for start or stop by corresponding start/stop (bat|sh) file.


3. Technologies
---------------

A new component framework based on spring, maven, which can keep your java application consistent between develop time and runtime.

Someone maybe doubt about, why there is another more wheel about component/plugin framework? There is OSGi framework already, and Spring DM server as application server even.

And springsource is developing a sub-project named as [spring-plugin](https://github.com/spring-projects/spring-plugin)

I'v tried to integrate those excellent opensource products in my app, but I found I'm stucked in OSGi terrable complexicity, especially integrated with my familar tools, such as IDE(Intellij), repository managment(Maven).

I think OSGi's complexity comes from runtime dynamic ability, it try to help me create a person who can cut off his leg and replace with another new one.

But I don't want so powerful and terrable man, I just need a normal man who can be borned, play and dead then.

I want it been component oriented, developer friendly, be consistent in anytime from any aspects.

So I tried to write the my products from user perspective:

1. Dependencies auto loaded
----------------

```bash
  java -jar path/to/my/app.jar arguments
```

All dependencies of the app.jar should be imported automatically after developer declaired in development time.

Because most of java developers use Maven as dependencies management tool, so do I.

Then we plan the app.jar should have a maven pom.xml declaired as:

```xml
  <dependencies>
    <depency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-core</artifactId>
      <version>3.2.14.RELEASE</version>
    </depency>
    <depency>
      <!--other more dependencies-->
    </depency>
  </dependencies>
```

as you know, if we want the app.jar startable by java -jar, it should contain a MANIFES.MF file declairs the Class-Path entry of this jar depends like those:

```properties
  Main-Class: my.app.Main
  Class-Path: path/to/spring-core-3.2.14.RELEASE.jar path/to/other-dependencies.jar
```

But they are duplicate with pom.xml apparently, and we need create this special jar for every new app.

so I decide to start the app.jar by another shared jar, it's our protagonist:

```bash
  java -jar spring.component.framework-0.0.1.jar path/to/my.app-1.0.0.jar
```

and the spring component framework jar will resolve the dependencies for the my.app-1.0.0.jar by resolving the pom.xml

In order to decouple the application with maven repository in runtime, the application should be deployed with all depended jars as below:

```
  path/to/app
    |-bin
    |  |-start.sh
    |  |-stop.sh
    |-boot
    |  |-spring.component.framework-0.0.1.jar
    |-lib
    |  |-my.app-1.0.0.jar
    |  |-org.springframework.spring-core-3.2.14.RELEASE.jar
    |  |-other.dependency-1.0.0.jar
```

3. Component oriented
----------------------

We think a component is a sealed-jar first.

And they can be summaried as 3 categories below:

### 3.1 Static Component

Static component should contain a pom.xml which is the maven's pom file in META-INF
and the framework will resolve dependencies from it.

It's used to provide static class to other dependencies.

a simple static component looks like:

```
  path/to/my.library.component-1.0.0.jar!
      |-META-INF
      |  |-pom.xml
      |  |-MANIFEST.MF
      |-my
      |  |-library
      |  |  |-Util.class
```

### 3.2 Application Component

The application component should contain an application.xml which is a spring application context file in META-INF besides pom.xml

and the framework will load this application context when start, unload the app context when stop vesa.

It's used to start some live-beans which can provide some live-functions or interacts with each other in runtime.

a simple application component looks like:

```
  path/to/my.application.component-1.0.0.jar!
      |-META-INF
      |  |-pom.xml
      |  |-application.xml
      |  |-MANIFEST.MF
      |-my
      |  |-app
      |  |  |-Bean.class
```

and the application.xml maybe looks like:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="my.app"/>
</beans>  
```

### 3.3 Service Component

One component can't be an application like one tree can't be a forest, we need several components work together, and we want they are separated in some way also to reduce system complexity.

So I make those rules:

1. Rule A:
  Application components are standalone from each other, that is to say, Jar A's application context shouldn't access Jar B's application context in any time, by any way.

2. Rule B:
  If they want to interact with each other, they are not a simple application component, the are upgrade as "Service Component"

  Service component is an application component with "service.xml" in META-INF.

3. Rule C:
  If a service component want to contribute some beans to other jar, they should export them as "service"
  such as:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<service xmlns="http://www.happyonroad.net/schema/service"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.happyonroad.net/schema/service
       http://www.happyonroad.net/schema/service.xsd">

    <export>
        <role>my.service.ServiceProvider</role>
        <hint>test</hint>
        <ref>testServiceProvider</ref>
    </export>

</service>
```

the xml element &lt;ref&gt;testServiceProvider&lt;/ref&gt; is refer to the bean in application context of this component.

4. Rule D:
  If a service component want to use some beans provided by other jars, they should import them as "service".

```xml
<?xml version="1.0" encoding="UTF-8"?>
<service xmlns="http://www.happyonroad.net/schema/service"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.happyonroad.net/schema/service
       http://www.happyonroad.net/schema/service.xsd">

    <import>
        <role>my.service.ServiceProvider</role>
        <as>serviceProvider</as>
    </import>

</service>
```
the xml element &lt;as&gt;serviceProvider&lt;/as&gt; make the imported service as a accessible bean by the application context of this component.

So the application the service component can use those imported service as local bean:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <bean class="my.service.Bean">
      <property name="dependedService" ref="serviceProvider"/>
    </bean>
</beans>  
```

Summary, service component A(provider)  looks like:

```
  path/to/my.service.provider-1.0.0.jar!
      |-META-INF
      |  |-pom.xml
      |  |-application.xml
      |  |-service.xml
      |  |-MANIFEST.MF
      |-my
      |  |-service
      |  |  |-ServiceProvider.class
      |  |-support
      |  |  |-ServiceProviderImpl.class
  
```

service component B(consumer) looks like:

```
  path/to/my.service.consumer-1.0.0.jar!
      |-META-INF
      |  |-pom.xml
      |  |-application.xml
      |  |-service.xml
      |  |-MANIFEST.MF
      |-my
      |  |-service
      |  |  |-Bean.class
  
```

