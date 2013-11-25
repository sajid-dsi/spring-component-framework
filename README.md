spring-component-framework
==========================

A new component framework based on spring, maven, which can keep your java application consistent between development and run time.

Someone will has doubt about, why there is another more wheel about component/plugin framework? there is OSGi as basic plugin framework, and Spring DM server as application server even.

I'v tried those excellent opensource products, but I found I'm stucked in OSGi terrable complexicity, especially integrated with my familar tools, such as IDE(Intellij), repository managment(Maven).

But I know my requiments clearly, I want to my java delivery be component oriented, developer friendly, consistent.

So I tried to write the my products as those:

1. Start Easily
----------------

```bash
  java -jar path/to/my/app.jar arguments
```

2. Dependencies auto loaded
---------------------------

All dependencies of the app should be imported automatically after developer declaired in development time.

So I choose maven as dependencies management tool.

the app.jar should have a maven pom.xml declair as:

```xml
  <dependencies>
    <depency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-core</artifactId>
      <version>3.2.14.RELEASE</version>
    </depency>
  </dependencies>
```

as you know, if we want achive this approach, we should have a MANIFES.MF file declairs the lib of this jar depends.

so I decide do not resolve these dependencies by the app.jar(that means all app.jar should be coded) and all the jars should be named as: $groupId.$artifactId-$version.jar

it should be started as:

```bash
  java -jar spring.component.framework-0.0.1.jar path/to/my.app-1.0.0.jar
```

and the spring component framework jar will resolve the dependencies for the my.app-1.0.0.jar

in order to decouple the application with maven repository in runtime, the application should be deployed with all depended jars as:

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

We should treat every standalone jar as a potential component, but the real component should follow those rules:

### 3.1 Static Component

the jar should has a pom.xml which is the maven's pom file in META-INF
and the framework will resolve dependencies for it.

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

the jar should has an application.xml which is a spring application context file in META-INF besides pom.xml
and the framework will load this application context.

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

One jar can't be an application like one tree can't be a forest, we need several jars work togher, but we need the are separated in some way also.

So I make those rules:

1. Rule A:
  Application components are standalone from each other, that is to say, Jar A's application context shouldn't access Jar B's application in any way and any time.

2. Rule B:
  If they want to interact with each other, they are not a simple static component, the are upgrade to "Service Component"

3. Rule C:
  If a service component want to contribute some beans to other jar, the should export them as "service"
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

4. Rule D:
  If a service component want to use some beans provided by other jars, they should import them as "service".

```
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

