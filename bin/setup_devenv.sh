#!/bin/bash -ex

docker rm -f mysql_autobet

docker run --name mysql_autobet -e MYSQL_ROOT_PASSWORD=mysql -e MYSQL_USER=mysql -e MYSQL_PASSWORD=mysql -d -p 13306:3306 mysql

sleep 20

echo "create database autobet;" | docker exec -i mysql_autobet mysql -pmysql mysql

./mvnw install -DskipTests

java -jar target/autobet-*-executable.jar load data/www*/mmz4281/ 
