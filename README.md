# 原始README

Maven原始 [README.md](./BUILD.md) 。

# 一些参考文章

+ [plexus-classwords介绍](./docresources/doc/01-plexus-classworlds.md)
+ [参考文章](./docresources/doc/REFERENCE.md)

# 基础构建

按照原始 [README.md](./BUILD.md) 中的说明：

> Quick Build
>
> If you want to bootstrap Maven, you'll need:
>
> + Java 1.7+
> + Maven 3.0.5 or later
> + Run Maven, specifying a location into which the completed Maven distro should be installed:
>
>> mvn -DdistributionTargetDir="$HOME/app/maven/apache-maven-3.6.x-SNAPSHOT" clean package

我在直接使用上述命令在本地构建：

```bash
mvn -DdistributionTargetDir="$HOME/app/maven/apache-maven-3.6.x-SNAPSHOT" clean package
```

构建成功后，会在`$HOME/app/maven/apache-maven-3.6.x-SNAPSHOT`这个文件夹中生成完整的Maven程序。

构建成功后，会在各个子模块的 target 目录下生成模块对应的class文件和其他构建产物。接下来即可使用IDEA调试项目了。 不进行此操作直接使用IDEA调试会各种【java: 找不到符号】问题。

> （一般不会遇到）注意：构建时如果出现`lifecycle-mapping`找不到的问题，我们可以参考：[这个网页](https://github.com/BINGOcoder1998/dummy-lifecycle-mapping-plugin) 。或者这样：
> ```bash
> git clone https://github.com/BINGOcoder1998/dummy-lifecycle-mapping-plugin.git
> cd dummy-lifecycle-mapping-plugin
> mvn install
> ```

# 源码调试

## 超级简单的调试方式-使用`mvnDebug`

我们在Maven的安装目录的bin文件夹下可以发现一个`mvnDebug`的脚本文件。

如果我们的本地Maven安装正确， 那么我们可以直接使用`mvnDebug`命令。它的使用方式和参数和`mvn`命令一致。在项目的根目录直接执行即可。

比如我们想要调试`clean`命令，那么我们任意找到一个Maven项目，在其根目录执行`mvnDebug clean`，即可 以调试方式启动Maven。

### 使用方式

#### 准备条件

+ 一个IDEA。
+ 任意一个Maven项目。
+ 已经安装好的Maven（需要知道版本）。可以执行`mvn`和`mvnDebug`命令。
+ 和安装好的Maven同版本的Maven源码。在这个地方下载
  [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) 。 可以下载src.tar.gz或者src.zip结尾的源码解压到本地。
  或者从github上拉取源码：`git clone https://github.com/apache/maven.git` 。

#### 调试步骤

1. 在IDEA中打开项目源码。
2. 在任意Maven项目中执行`mvnDebug clean`命令。 此时，我们可以看到在命令行里如下输出：

   ![mvnDebug-1](./docresources/img/mvn-debug-start.png)
3. 在IDEA中新增远程调试启动项。如下：

   ![mvnDebug-2](./docresources/img/mvn-debug-idea-config.png)
4. Maven入口代码处打上断点。在`org.codehaus.plexus.classworlds.launcher.Launcher.main`方法内开始 处打上断点。
5. 点击IDEA的Debug图标：

   ![mvDebug-3](./docresources/img/mvn-debug-idea-start.png)

   接下来即可看到已经进入到断点：

   ![mvDebug-4](./docresources/img/mvn-debug-idea-breakpoint.png)

注意：

+ 调试的输出会在执行`mvnDebug clean`的Terminal中输出。而不会在IDEA的Console中输出。
+ 这种调试方式当我们修改了源码不会再调试过程中生效。

如果想要修改源码并在调试中测试，则可以使用下文的调试方式。

## 寻找启动类

如何找到启动类？
> TODO 通过命令执行文件

启动类路径：

```java
org.codehaus.plexus.classworlds.launcher.Launcher#main
```

启动类内容如下：

```java
    //@formatter:off
    public static void main( String[] args )
    {
        try
        {
            int exitCode = mainWithExitCode( args );

            System.exit( exitCode );
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            System.exit( 100 );
        }
    }
    //@formatter:on
```

## 开始调试

### 新建启动辅助类

我们新建一个module，帮助我们启动 Maven 源码以便调试。

1. 使用 IDEA 在当前根目录新建一个 Module，名称为`maven-example`。（新 module 的 pom 参考`maven-example/pom.xml`）
2. 新建启动辅助类`MavenStarter.java`。

`MavenStarter.java`包含一个main方法， 帮助我们启动当前Maven源码以便调试。代码内容如下:

```java
package org.apache.maven;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven source code debug entrance.
 */
public class MavenStarter {
    private static final Logger LOG = LoggerFactory.getLogger(MavenStarter.class);

    public static void main(String[] args) {
        LOG.info("===================== Program Arguments ========================");
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = bean.getInputArguments();

        for (String arg : arguments) {
            LOG.info(arg);
        }
        LOG.info("===================== Program Arguments ========================");
        // 核心代码：当前类引用org.codehaus.plexus.classworlds.launcher.Launcher的main方法
        // 以启动Maven。
        org.codehaus.plexus.classworlds.launcher.Launcher.main(args);
    }
}

```

接下来我们可以使用这个类来辅助我们启动 maven 源码进行调试了。

在此之前，我们会遇到两个问题：

1. 启动参数配置
2. classpath 路径配置

在下文中我们会一一处理它们。

### 如何找到启动参数？

> TODO源码or命令行文件

```bash
exec "$JAVACMD" \
  $MAVEN_OPTS \
  $MAVEN_DEBUG_OPTS \
  -classpath "${CLASSWORLDS_JAR}" \
  "-Dclassworlds.conf=${MAVEN_HOME}/bin/m2.conf" \
  "-Dmaven.home=${MAVEN_HOME}" \
  "-Dlibrary.jansi.path=${MAVEN_HOME}/lib/jansi-native" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  ${CLASSWORLDS_LAUNCHER} "$@"

```

### jvm 启动参数

注意，以下出现的目录中：

+ `/Users/abc/CodeSoftware/maven/apache-maven-3.8.5`（即下文【已安装的maven目录】）是安装在当前系统上可以直接使用的Maven。
+ `/Users/abc/IdeaProjects/apache-maven-3.8.5`（即下文【当前的源码包目录】）是Maven的源码目录，也就是我们当前想要调试的Maven源码的目录。
+ 【目标项目根目录】指的是Maven要操作的项目根目录，可以是任何一个Maven项目的目录。 它们可以是不同版本的Maven。注意不要混淆

参数详解：

+ classpath
    + classpath 不需要，在IDEA环境中会自动加载好此参数。这也是我们能够调试maven源码的关键。


+ -Dclassworlds.conf
    + maven启动时的classworlds配置，【当前的源码包目录】里没有，我们直接使用【已安装的maven目录】的配置。

```bash
-Dclassworlds.conf=/Users/abc/CodeSoftware/maven/apache-maven-3.8.5/bin/m2.conf
```

+ -Dmaven.home
    + maven的安装路径。理论上来说就是我们的【当前的源码包目录】，考虑到maven运行时可能会从此路径获取一些 源码中不存在的配置文件，因此为了简单我们直接将其配置为【已安装的maven目录】。

```bash
-Dmaven.home=/Users/abc/CodeSoftware/maven/apache-maven-3.8.5/
```

+ -Dlibrary.jansi.path
    + 用于在Windows下处理ansi的库的位置，非Windows可以不用配置。

```bash
-Dlibrary.jansi.path=/Users/abc/CodeSoftware/maven/apache-maven-3.8.5/lib/jansi-native/
```

+ -Dmaven.multiModuleProjectDirectory
    + Maven扩展功能配置文件所在的路径，一般为【目标项目根目录】。Maven会加载此目录的`.mvn/extensions.xml`文件，
      根据此文件配置执行扩展。参考 [core-extensions](https://maven.apache.org/docs/3.3.1/release-notes.html#core-extensions) 。

```bash
-Dmaven.multiModuleProjectDirectory=/Users/abc/IdeaProjects/demo/
```

我们汇总一下在IDEA中启动`org.codehaus.plexus.classworlds.launcher.Launcher.main`需要的参数：

```bash
-Dclassworlds.conf=/Users/abc/CodeSoftware/maven/apache-maven-3.8.5/bin/m2.conf -Dmaven.home=/Users/abc/CodeSoftware/maven/apache-maven-3.8.5/ -Dlibrary.jansi.path=/Users/abc/CodeSoftware/maven/apache-maven-3.8.5/lib/jansi-native/ -Dmaven.multiModuleProjectDirectory=/Users/abc/IdeaProjects/apache-maven-3.8.5
```

我们将以上参数添加到IDEA的中“VM Options”中，使其成为jvm参数。供程序运行时读取。

### Program Arguments 程序参数

另外我们需要给它添加Maven执行时需要的参数参数。假如我们需要调试一下`mvn validate`方式对应的源码执行过程。那么我们需要将`validate`
添加到`Program Arguments`中即可。之后我们如果需要测试不同的maven参数，修改这个值即可。

+ 传递给Maven的待执行的命令【1】
    + 比如平时我们使用`mvn validate`执行清理工作，那么此时我们就要将`validate`配置在`Program Arguments`中。

```bash
validate
```

+ 传递给Maven的待执行的命令【2】
    + 或者我们需要调试`mvn clean install -DskipTests`命令，那么此时我们就要将`clean install -DskipTests`配置在`Program Arguments`中。

```bash
clean install -DskipTests
```

+ （可选）-f
    + 由于我们是使用IDEA调试Maven源码，默认Maven的操作是对当前源码目录执行的。 因此我们想调试时让源码执行对另外的Maven项目操作，则使用这个参数指定项目的路径 或`pom.xml`
      文件的路径。（注意，此参数应该和`-Dmaven.multiModuleProjectDirectory`
      保持一致）

```bash
-f /Users/abc/IdeaProjects/spring-boot-demo/
```

汇总一下`Program Arguments`的参数配置。

```bash
clean install -DskipTests -f /Users/abc/IdeaProjects/spring-boot-demo/
```

整个配置如下图所示：

![IDEA 启动配置](./docresources/idea-startup-config.png "IDEA 启动配置")

### classpath 配置

一般情况下，IDEA会自动给我们处理好项目依赖和 classpath。但是在实际调试中有可能会出现 加载的 classpath 不完整导致的启动出错。

因此我们在此介绍一下如何在IDEA下处理 classpath。

> 首先，我们在`maven-example`的`pom`中依赖了所有的Maven子module。这样做是为了在 代码执行过程中，IDEA会直接执行具体module的`target/classes`下的class，而不是去 执行对应jar包的代码，方便我们修改完源码后观察代码行为。

> 其次，有了上面的配置IDEA应该能够正常的处理classpath了，但是如果还存在类找不大的问 题。那么请打开IDEA的`Project Structure`配置面板（Field-> Project Structure）, 找到`Project Settings -> Modules`，然后选择`maven-example`module，再选择
`dependencies`；点击页面的“+”号，选择【1 JARs or Directories】，选择某个module 下的`target/classes/`文件夹并添加保存。重复这个操作，直到所有的module都添加完成。

![添加文件夹到classpath](./docresources/add-dir-to-classpath.png "添加文件夹到classpath")

OK，这个方法太笨了。有个偷懒的方法，就是将以下代码复制你本地的`maven-example/maven-example.iml`文件中。 这个操作等效于我们在配置页面手动添加文件夹到module中。

```xml

<CLASSES>
    <root url="file://$MODULE_DIR$/../maven-artifact/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-builder-support/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-compat/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-core/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-embedder/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-model/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-model-builder/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-plugin-api/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-repository-metadata/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-resolver-provider/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-settings/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-settings-builder/target/classes"/>
    <root url="file://$MODULE_DIR$/../maven-slf4j-provider/target/classes"/>
</CLASSES>
```

`maven-example/maven-example.iml`的整个文件内容可以参考`docresources/helper/maven-example.iml.bak.xml`文件。
但是其实我们只关心上面代码片段中列出的`CLASSES`节点下的配置就可以了。

至此，我们即可使用IDEA启动项目。但是可能会出现问题，请参考下文【启动时问题修复】。

# 启动时问题修复

1. [ERROR] Failed to execute goal org.apache.maven.plugins:maven-enforcer-plugin:3.0.0:enforce (
   enforce-bytecode-version) on project maven: The plugin org.apache.maven.plugins:maven-enforcer-plugin:3.0.0 requires
   Maven version 3.1.1 -> [Help 1]

+ maven启动时`org.apache.maven.rtinfo.internal.DefaultRuntimeInformation.getMavenVersion`
  方法会从`META-INF/maven/org.apache.maven/maven-core/pom.properties`文件内读取当前软件版本号。本项目目前配置的默认值时`2.1-SNAPSHOT`
  。故此时校验会出错。因为当前代码的版本为`3.8.5`， 因此我们将版本改为`3.8.5`即可。
+ 修改1：
    + 修改文件路径为：`maven-core/src/test/resources/META-INF/maven/org.apache.maven/maven-core/pom.properties`
    + 修改文件内容为：

```text
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

#Generated by Maven
#Tue May 14 22:11:42 EDT 2022
version=3.8.5
groupId=org.apache.maven
artifactId=maven-core
```

+ 修改2：
    + 修改文件路径为：`maven-embedder/src/test/resources/META-INF/maven/org.apache.maven/maven-core/pom.properties`
    + 修改文件内容为：

```text
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

#Generated by Maven
#Tue May 14 22:11:42 EDT 2022
version=3.8.5
groupId=org.apache.maven
artifactId=maven-embedder
```

2. [ERROR] Failed to execute goal org.apache.rat:apache-rat-plugin:0.13:check (rat-check) on project maven: Too many
   files with unapproved license: 1 See RAT report in: /Users/abc/IdeaProjects/apache-maven-3.8.5/target/rat.txt
   -> [Help 1]

+ 这是因为我们部分文件没有添加`license`声明所致。我们可以选择给自己新增的文件添加`licence`声明或者将新增的文件或目录忽略掉不进行检查。我们使用如下方式进行忽略：

```xml
          <!--@formatter:off-->
          <!--此文件为根目录下的pom.xml文件，此插件在pluginManagement下管理-->

          <groupId>org.apache.rat</groupId>
          <artifactId>apache-rat-plugin</artifactId>
          <configuration>
            <excludes>
              <exclude>src/test/resources*/**</exclude>
              <exclude>src/test/projects/**</exclude>
              <exclude>src/test/remote-repo/**</exclude>
              <exclude>**/*.odg</exclude>
              <!--
                ! Excluded the license files itself cause they do not have have a license of themselves.
              -->
              <exclude>src/main/appended-resources/licenses/EPL-1.0.txt</exclude>
              <exclude>src/main/appended-resources/licenses/unrecognized-javax.annotation-api-1.2.txt</exclude>
              <!--
                ! This is a file I created to record source code reading, no license is required.
              -->
              <exclude>BUILD.md</exclude>
              <exclude>docresources/**</exclude>
            </excludes>
          </configuration>
          <!--@formatter:off-->
```

# ~~添加日志依赖~~(不需要这一步，我们使用自带的logback日志即可)

启动项目后我们发现此时没有正常的日志依赖。因此，我们添加日志相关依赖。

+ 在源码根目录的`pom.xml`文件中添加依赖如下：

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>2.17.2</version>
</dependency>
```

同时在`maven-compat/src/test/resources/`文件夹下添加`log4j2.xml`文件。 内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
    <Appenders>
        <Console name="CONSOLE" target="SYSTEM_OUT">
            <PatternLayout
                    pattern="%style{%d}{bright,white} %highlight{%-5p} [%style{%t}{bright,blue}] [%style{%40.40C}{bright,yellow}:%style{%4.4L}{bright,yellow}] %style{[%m]%n}{cyan}%style{%throwable}{red}"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="CONSOLE"/>
        </Root>

    </Loggers>
</Configuration>
```

至此，日志依赖配置完成。