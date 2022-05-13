# 基础构建

按照 [README.md](./README.md) 中的说明：

> Quick Build
>
> If you want to bootstrap Maven, you'll need:
>
> + Java 1.7+
> + Maven 3.0.5 or later
> + Run Maven, specifying a location into which the completed Maven distro should be installed:
> 
>> mvn -DdistributionTargetDir="$HOME/app/maven/apache-maven-3.6.x-SNAPSHOT" clean package

我在本地构建时使用了如下命令：
```bash
mvn -DdistributionTargetDir="~/IdeaProjects/apache-maven-3.8.5-SNAPSHOT" clean package
```
注意 distributionTargetDir 指定的文件夹需要已存在，否则报错。

构建成功后，会在各个子模块的 target 目录下生成模块对应的jar包。