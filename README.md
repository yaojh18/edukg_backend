# edukg_backend

## 1 后端平台使用
### 1.0 mysql权限设置
我mysql版本是8.0.13
```shell
mysql -u root -p; #登录
create database springboot_edukg;
create user 'springboot'@'%' identified by 'springboot';
grant all privileges on springboot_edukg.* to 'springboot'@'%' with grant option;
flush privileges;
```
### 1.1 使用IDEA运行  
将File->settings->Editor->File encodings中properties files的transparent
native-to-ascii conversion一项勾选，并将字符集改成utf-8
点击右上角的run运行
### 1.2 使用shell运行
确保安装jdk和maven,执行以下命令
```shell
mvn package 
cd target 
java -jar edukg_backend-0.0.1-SNAPSHOT.jar # jar文件名称可能不同，最后一个参数为target文件夹下生成的jar文件
```
