#edukg_backend

## 1 Backend platform usage
### 1.0 mysql permission settings
My mysql version is 8.0.13
```shell
mysql -u root -p; #login
create database springboot_edukg;
create user 'springboot'@'%' identified by 'springboot';
grant all privileges on springboot_edukg.* to 'springboot'@'%' with grant option;
flush privileges;
````
### 1.1 Running with IDEA
Transparent the properties files in File->settings->Editor->File encodings
native-to-ascii conversion is checked and the character set is changed to utf-8
Click run in the upper right corner to run
### 1.2 Running with shell
Make sure to install jdk and maven, execute the following commands
```shell
mvn package
cd target
java -jar edukg_backend-0.0.1-SNAPSHOT.jar # The name of the jar file may be different, the last parameter is the jar file generated in the target folder
````