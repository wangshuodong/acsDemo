- 开发环境

启动时配置jvm参数：-Drms.config.path=/your_path/rms.properties -Dlogback.configurationFile=/your_path/logback.xml

- 正式环境

在tomcat中bin/catalina.sh下增加如下配置：
JAVA_OPTS="-Drms.config.path=/your_path/rms.properties -Dlogback.configurationFile=/your_path/logback.xml"