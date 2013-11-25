spring-component-framework
==========================

A new component framework based on spring, maven, which can keep your java application consistent between develop time and runtime.

Someone maybe doubt about, why there is another more wheel about component/plugin framework? There is OSGi framework already, and Spring DM server as application server even.

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

