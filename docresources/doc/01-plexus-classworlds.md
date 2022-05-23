# plexus-classworlds

## 简介

[plexus-classworlds 官网](https://codehaus-plexus.github.io/plexus-classworlds/) 。

Plexus Classworlds 是一个适用于需要对 Java 的 ClassLoaders 进行复杂操作的容器开发人员的框架。 Java 的原生 ClassLoader
机制和类可能会给某些类型的应用程序开发人员带来很多麻烦和困惑。涉及动态加载组件或以其他方式表示“容器”的项目可以从 Classworlds 提供的类加载控制中受益。

Plexus Classworlds 为类加载提供了比 Java 的正常机制更丰富的语义集，同时仍然能够提供 ClassLoader 接口以与 Java 环境无缝集成。

Classworlds 模型取消了通常与 ClassLoader 相关联的层次结构。相反，ClassWorld 提供了一个 ClassRealms 池，可以从其他 ClassRealms 导入任意包。实际上，Classworlds
将旧式层次结构转变为有向图。

在应用程序容器环境中，容器可能具有能够加载容器/组件的契约接口和类的`realm`。 为从容器`realm`导入契约类的每个组件创建另一个`realm`。

该模型允许对哪个类加载器加载任何特定类进行细粒度控制。这种形式的部分隔离可以减少从多个加载器加载类所产生的无数奇怪错误。

此外，`Plexus Classworlds` 提供了一个启动器来帮助从配置文件创建类加载器和 ClassRealms，以及从通过正确的类加载器加载的正确类启动应用程序的 main 方法。

[from this](https://codehaus-plexus.github.io/plexus-classworlds/index.html)

### 关于 Launcher

`Launcher` 是 `Classworlds` 提供的一个启动器。当我们在配置文件中配置了main方法的相关信息后，它可以帮助我们根据配置启动指定的方法。

### 目的

为了减少类加载项目的数量，Plexus Classworlds 取代了forehead用于应用程序的启动。 （Classworlds 支持使用`Launcher`+配置文件的方式启动应用程序）

应用程序启动要解决的主要问题包括定位应用程序的所有 JAR、配置初始类加载器和调用`main`入口方法。

Classworlds
的[启动器工具](https://codehaus-plexus.github.io/plexus-classworlds/apidocs/index.html?org/codehaus/plexus/classworlds/launcher/package-summary.html)
简化了定位应用程序 jar 的过程。一个常见的习惯用法是有一个脚本，它只在类路径中使用 `plexus-classworlds.jar` 和一个指定启动器配置的位置的系统属性来启动 JVM。
此外，通常在命令行上传递指定应用程序安装位置的属性。

```bash
$JAVA_HOME/bin/java \
    -classpath $APP_HOME/boot/plexus-classworlds-2.5.2.jar \
    -Dclassworlds.conf=$APP_HOME/etc/classworlds.conf \
    -Dapp.home=$APP_HOME \
    org.codehaus.plexus.classworlds.launcher.Launcher \
    $*
```

+ `$APP_HOME/boot/plexus-classworlds-2.5.2.jar`是`Classworlds`的jar包。
+ `$APP_HOME/etc/classworlds.conf`是`Classworlds`的启动配置文件。里面标识了需要启动的类信息和需要加载的jar包信息。
+ `$APP_HOME`是要运行的程序根目录。
+ `org.codehaus.plexus.classworlds.launcher.Launcher`即为启动器的类路径。
+ `$*` 将命令行的参数传递给启动类。

## 配置启动文件

### 入口点定义

在指定`realm`定义之前，必须使用 main is 指令指定入口点类和领域。

```bash
main is com.werken.projectz.Server from app
```

其中`main`代表我们定义的是main方法。`com.werken.projectz.Server`是main方法所在的类。`app`是main方法所在的realm。 realm我们在下文定义。

### 系统属性定义

系统属性可以在入口点之前和之后，但在`realm`之前设置：

```bash
set <property> [[using <properties filename>]] [[default <default value>]]
```

### Realm 定义

在配置文件中必须至少定义一个Classworlds realm。 开始定义`realm`的语法是[realm.name]。在`realm`标题之后的所有行都被视为该`realm`的指令。
`realm`定义一直持续到另一个`realm`被定义或到达文件的末尾为止。

```bash
[realm.one]
    ...
    ...
[realm.two]
    ...
    ...
[realm.three]
    ...
    ...
```

在一个`realm`定义中，有三个指令可用：`load`、`optionally`和`import`。

`load`和`optionally`指令指定了一个用于在`realm`中加载类的源：唯一的区别是在没有源的情况下，load会失败，optionally则不会。 任何在文件名中含有星号（`*`
）的加载源都会被与文件名前缀和后缀相匹配的文件列表所取代。 系统属性可以用`${propname}`的符号来引用。`load`和`optionally`指令等同于`ClassRealm`的`addURL(..)`方法。

```bash
[app]
    load ${app.home}/lib/*.jar
    optionally ${app.home}/lib/ext/*.jar
    load ${tools.jar}
```

`import`指令指定某些包应该通过另一个`realm`的方式被导入和加载。`import`指令等同于`ClassRealm`的`importFrom(..)`方法。

```bash
[app]
    ...
  
[subcomponent]
    import com.werken.projectz.Foo from app
    ...
```

### 入口点方法

`Classworlds`可以用来调用任何现有应用程序的`main()`方法。使用标准的入口点不允许获得应用程序的`ClassWorld`，但不是所有的应用程序在运行时都需要`Classworlds`。

对于那些确实需要`ClassWorld`实例的应用程序，可以提供一个替代的入口点方法签名。只需在`main`的主参数列表中添加一个`ClassWorld`参数。

```bash
public class MyApp
{
    public static void main( String[] args, ClassWorld world )
    {
        ...     
    }
}
```

## Maven中`Classworlds`的配置文件解析

我们在介绍Maven的启动参数时，配置的`Classworlds`相关的参数：

```bash
-Dclassworlds.conf=/Users/abc/CodeSoftware/maven/apache-maven-3.8.5/bin/m2.conf
```

我们可以在Maven的安装目录下的`bin/m2.conf`找到这个配置文件，如下：

```bash
main is org.apache.maven.cli.MavenCli from plexus.core

set maven.conf default ${maven.home}/conf

[plexus.core]
load       ${maven.conf}/logging
optionally ${maven.home}/lib/ext/*.jar
load       ${maven.home}/lib/*.jar
```

我们可以看到，Maven设置了一个`realm`：`plexus.core`。 并且指定启动类为`org.apache.maven.cli.MavenCli`。 并且配置了一个换行变量`maven.conf`
为`${maven.home}/conf`。 指定了3个类加载的目录`${maven.conf}/logging`、`${maven.home}/lib/ext/*.jar`和`${maven.home}/lib/*.jar`。

因此，Maven启动过程为：

```
org.codehaus.plexus.classworlds.launcher.Launcher -> org.apache.maven.cli.MavenCli
```

## Classworlds API 使用方式

Java API可以用来创建新的`realm`，并通过导入特定的包将`realm`连接在一起。

`Classworlds`
基础设施的核心是 [ClassWorld](https://codehaus-plexus.github.io/plexus-classworlds/apidocs/index.html?org/codehaus/plexus/classworlds/ClassWorld.html)
类。一个应用程序必须创建一个`ClassWorld`实例。最好是将该实例存储为一个单例或其他方便的位置。

```java
    ClassWorld world=new ClassWorld();
```

一旦一个`ClassWorld`被创建，就可以在其中创建`realm`。这些`realm`实际上只允许加载JVM的核心类。

```java
    ClassWorld world=new ClassWorld();
    ClassRealm containerRealm=world.newRealm("container");
    ClassRealm logComponentRealm=world.newRealm("logComponent");
```

为了使每个`ClassRealm`都有用，必须添加一些成分，以使每个都能提供某些类。

```java
    containerRealm.addURL(containerJarUrl);
    logComponentRealm.addURL(logComponentJarUrl);
```

现在，需要创建各个`realm`之间的链接，以允许从一个`realm`加载的类可用于另一个`realm`中加载的类。

```java
    logComponentRealm.importFrom("container","com.werken.projectz.component");
```

然后可以从其`realm`加载容器实现并使用。

```java
    Class containerClass=containerRealm.loadClass(CONTAINER_CLASSNAME);
    MyContainer container=(MyContainer)containerClass.newInstance();
    Thread.currentThread().setContextClassLoader(containerRealm.getClassLoader());
    container.run();
```

理想情况下，容器本身将负责为每个被加载的组件创建一个`ClassRealm`，并将组件的契约接口导入组件的`ClassRealm`，然后使用`loadClass(...)`来进入沙盒组件`realm`。