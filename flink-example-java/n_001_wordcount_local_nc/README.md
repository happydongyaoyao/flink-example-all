# flink1.9.1 java版初体验

## 简介
- window 单机环境
- nc 做为数据源发送数据
- flink 流按时间间隔100毫秒统计单词

## 源码
- https://github.com/opensourceteams/flink-example-java/tree/master/n_001_wordcount_local_nc

## 更多资源
- https://github.com/opensourceteams/flink-example-all

## nc 命令发送数据
- 输入命令后处理阻塞状态，等待连通后,就可以输入数据，按回车可以发送数据

 `nc -l -p 1234`

## 输入数据
    c b a b c b

## 输出数据


    c:2 
    a:1 
    b:3 
## flink maven java版 maven命令自动生成
     mvn archetype:generate -DarchetypeGroupId=org.apache.flink -DarchetypeArtifactId=flink-quickstart-java -DarchetypeVersion=1.9.1 -DgroupId=opensourceteams -DartifactId=flink-example-java -Dversion=0.1 -Dpackage=com.opensourceteams.bigdata.flink.example -DinteractiveMode=false

## 源码

### 1、按行输出数据
- 输出数据

	`c b a b c b`

```java
package com.opensourceteams.bigdata.flink.example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class n_001_NCLocalRun {


    public static void main(String[] args) throws Exception {

        final  int port = 1234 ;
        // get the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // get input data by connecting to the socket
        DataStream<String> text = env.socketTextStream("localhost", port, "\n");


        text.print().setParallelism(1);

        env.execute("输出nc终端数据") ;


    }
}

```
### 2、按行输出数据(按word拆分)
- 输出数据


    c
    b
    a
    b
    c
    b

```java
package com.opensourceteams.bigdata.flink.example;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class n_002_NCLocalRun_SplitWord {

    public static void main(String[] args) throws Exception {

        final  int port = 1234 ;
        // get the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // get input data by connecting to the socket
        DataStream<String> text = env.socketTextStream("localhost", port, "\n");


        DataStream<String> wordDataStream =  text.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                System.out.println("输入的一行数据:"+s);
                // for(String word : s.split("\\\\s")){
                for(String word : s.split(" ")){
                    collector.collect(word);
                }

            }
        })
                ;


        System.out.println("=========================打印的数据=========================");
        wordDataStream.print().setParallelism(1);

        env.execute("输出nc终端数据") ;



    }
}

```

### 3、单词统计(按word count 拆分)
- 输出数据


    b:3 
    c:2 
    a:1 

```java
package com.opensourceteams.bigdata.flink.example;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class n_005_NCLocalRun_WordCount_window {

    public static void main(String[] args) throws Exception {

        final  int port = 1234 ;
        // get the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // get input data by connecting to the socket
        DataStream<String> text = env.socketTextStream("localhost", port, "\n");


        DataStream<WordWithCount> wordDataStream =  text.flatMap(new FlatMapFunction<String, WordWithCount>() {
            @Override
            public void flatMap(String s, Collector<WordWithCount> collector) throws Exception {
                System.out.println("输入的一行数据:"+s);
                // for(String word : s.split("\\\\s")){
                for(String word : s.split(" ")){
                    //一次数据
                    collector.collect(new WordWithCount(word,1L));
                }

            }
        })
                ;


        DataStream<WordWithCount> wordCount =  wordDataStream.keyBy("word")
                .timeWindow(Time.milliseconds(100))
                .reduce(new ReduceFunction<WordWithCount>() {
                    @Override
                    public WordWithCount reduce(WordWithCount a, WordWithCount b) throws Exception {
                        return new WordWithCount(a.getWord(),a.getCount() + b.getCount());
                    }
                })
                ;


        wordCount.print().setParallelism(1);

        env.execute("输出nc终端数据") ;



    }


}

```

### pom.xml
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    	<modelVersion>4.0.0</modelVersion>
    
    	<groupId>opensourceteams</groupId>
    	<artifactId>opensourceteams</artifactId>
    	<version>0.1</version>
    	<packaging>jar</packaging>
    
    	<name>Flink Quickstart Job</name>
    	<url>http://www.myorganization.org</url>
    
    	<properties>
    		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    		<flink.version>1.9.1</flink.version>
    		<java.version>1.8</java.version>
    		<scala.binary.version>2.11</scala.binary.version>
    		<maven.compiler.source>${java.version}</maven.compiler.source>
    		<maven.compiler.target>${java.version}</maven.compiler.target>
    	</properties>
    
    	<repositories>
    		<repository>
    			<id>apache.snapshots</id>
    			<name>Apache Development Snapshot Repository</name>
    			<url>https://repository.apache.org/content/repositories/snapshots/</url>
    			<releases>
    				<enabled>false</enabled>
    			</releases>
    			<snapshots>
    				<enabled>true</enabled>
    			</snapshots>
    		</repository>
    	</repositories>
    
    	<dependencies>
    		<!-- Apache Flink dependencies -->
    		<!-- These dependencies are provided, because they should not be packaged into the JAR file. -->
    		<dependency>
    			<groupId>org.apache.flink</groupId>
    			<artifactId>flink-java</artifactId>
    			<version>${flink.version}</version>
    			<scope>provided</scope>
    		</dependency>
    		<dependency>
    			<groupId>org.apache.flink</groupId>
    			<artifactId>flink-streaming-java_${scala.binary.version}</artifactId>
    			<version>${flink.version}</version>
    			<scope>provided</scope>
    		</dependency>
    
    <!--		<dependency>
    			<groupId>org.apache.flink</groupId>
    			<artifactId>flink-connector-wikiedits_2.11</artifactId>
    			<version>${flink.version}</version>
    		</dependency>-->
    
    		<!-- Add connector dependencies here. They must be in the default scope (compile). -->
    
    		<!-- Example:
    
    		<dependency>
    			<groupId>org.apache.flink</groupId>
    			<artifactId>flink-connector-kafka-0.10_${scala.binary.version}</artifactId>
    			<version>${flink.version}</version>
    		</dependency>
    		-->
    
    		<!-- Add logging framework, to produce console output when running in the IDE. -->
    		<!-- These dependencies are excluded from the application JAR by default. -->
    		<dependency>
    			<groupId>org.slf4j</groupId>
    			<artifactId>slf4j-log4j12</artifactId>
    			<version>1.7.7</version>
    			<scope>runtime</scope>
    		</dependency>
    		<dependency>
    			<groupId>log4j</groupId>
    			<artifactId>log4j</artifactId>
    			<version>1.2.17</version>
    			<scope>runtime</scope>
    		</dependency>
    	</dependencies>
    
    	<build>
    		<plugins>
    
    			<!-- Java Compiler -->
    			<plugin>
    				<groupId>org.apache.maven.plugins</groupId>
    				<artifactId>maven-compiler-plugin</artifactId>
    				<version>3.1</version>
    				<configuration>
    					<source>${java.version}</source>
    					<target>${java.version}</target>
    				</configuration>
    			</plugin>
    
    			<!-- We use the maven-shade plugin to create a fat jar that contains all necessary dependencies. -->
    			<!-- Change the value of <mainClass>...</mainClass> if your program entry point changes. -->
    			<plugin>
    				<groupId>org.apache.maven.plugins</groupId>
    				<artifactId>maven-shade-plugin</artifactId>
    				<version>3.0.0</version>
    				<executions>
    					<!-- Run shade goal on package phase -->
    					<execution>
    						<phase>package</phase>
    						<goals>
    							<goal>shade</goal>
    						</goals>
    						<configuration>
    							<artifactSet>
    								<excludes>
    									<exclude>org.apache.flink:force-shading</exclude>
    									<exclude>com.google.code.findbugs:jsr305</exclude>
    									<exclude>org.slf4j:*</exclude>
    									<exclude>log4j:*</exclude>
    								</excludes>
    							</artifactSet>
    							<filters>
    								<filter>
    									<!-- Do not copy the signatures in the META-INF folder.
    									Otherwise, this might cause SecurityExceptions when using the JAR. -->
    									<artifact>*:*</artifact>
    									<excludes>
    										<exclude>META-INF/*.SF</exclude>
    										<exclude>META-INF/*.DSA</exclude>
    										<exclude>META-INF/*.RSA</exclude>
    									</excludes>
    								</filter>
    							</filters>
    							<transformers>
    								<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
    									<mainClass>wikiedits.StreamingJob</mainClass>
    								</transformer>
    							</transformers>
    						</configuration>
    					</execution>
    				</executions>
    			</plugin>
    		</plugins>
    
    		<pluginManagement>
    			<plugins>
    
    				<!-- This improves the out-of-the-box experience in Eclipse by resolving some warnings. -->
    				<plugin>
    					<groupId>org.eclipse.m2e</groupId>
    					<artifactId>lifecycle-mapping</artifactId>
    					<version>1.0.0</version>
    					<configuration>
    						<lifecycleMappingMetadata>
    							<pluginExecutions>
    								<pluginExecution>
    									<pluginExecutionFilter>
    										<groupId>org.apache.maven.plugins</groupId>
    										<artifactId>maven-shade-plugin</artifactId>
    										<versionRange>[3.0.0,)</versionRange>
    										<goals>
    											<goal>shade</goal>
    										</goals>
    									</pluginExecutionFilter>
    									<action>
    										<ignore/>
    									</action>
    								</pluginExecution>
    								<pluginExecution>
    									<pluginExecutionFilter>
    										<groupId>org.apache.maven.plugins</groupId>
    										<artifactId>maven-compiler-plugin</artifactId>
    										<versionRange>[3.1,)</versionRange>
    										<goals>
    											<goal>testCompile</goal>
    											<goal>compile</goal>
    										</goals>
    									</pluginExecutionFilter>
    									<action>
    										<ignore/>
    									</action>
    								</pluginExecution>
    							</pluginExecutions>
    						</lifecycleMappingMetadata>
    					</configuration>
    				</plugin>
    			</plugins>
    		</pluginManagement>
    	</build>
    
    	<!-- This profile helps to make things run out of the box in IntelliJ -->
    	<!-- Its adds Flink's core classes to the runtime class path. -->
    	<!-- Otherwise they are missing in IntelliJ, because the dependency is 'provided' -->
    	<profiles>
    		<profile>
    			<id>add-dependencies-for-IDEA</id>
    
    			<activation>
    				<property>
    					<name>idea.version</name>
    				</property>
    			</activation>
    
    			<dependencies>
    				<dependency>
    					<groupId>org.apache.flink</groupId>
    					<artifactId>flink-java</artifactId>
    					<version>${flink.version}</version>
    					<scope>compile</scope>
    				</dependency>
    				<dependency>
    					<groupId>org.apache.flink</groupId>
    					<artifactId>flink-streaming-java_${scala.binary.version}</artifactId>
    					<version>${flink.version}</version>
    					<scope>compile</scope>
    				</dependency>
    			</dependencies>
    		</profile>
    	</profiles>
    
    </project>
  
## maven 运行
- classpath设置flink的引用包
- classpath:flink-1.9.1\lib\flink-dist_2.11-1.9.1.jar
```aidl
mvn clean package
java -cp target\opensourceteams-0.1.jar;%CLASSPATH% com.opensourceteams.bigdata.flink.example.n_005_NCLocalRun_WordCount_window

$ mvn exec:java -Dexec.mainClass=com.opensourceteams.bigdata.flink.example.n_005_NCLocalRun_WordCount_window
```    
## 引用

### window 安装nc
- https://github.com/opensourceteams/flink-example-java/blob/master/n_001_wordcount_local_nc/md/nc.md