#!/bin/bash -ex

echo "Creating new devenv for autobet"

docker rm -f mysql_autobet || true

docker run --name mysql_autobet -e MYSQL_ROOT_PASSWORD=mysql -e MYSQL_USER=mysql -e MYSQL_PASSWORD=mysql -d -p 13306:3306 kokosing/mysql

./mvnw install -DskipTests -q

sleep 10

echo "create database autobet;" | docker exec -i mysql_autobet mysql -pmysql mysql

java -jar target/autobet-*-executable.jar load data/www*/mmz4281/ 
