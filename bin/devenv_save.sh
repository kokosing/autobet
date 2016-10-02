#!/bin/bash -ex

if ! docker ps  | grep -q mysql_autobet$; then
  echo devenv is not started
  exit
fi


docker rm -f mysql_autobet_tmp | true
docker run --name mysql_autobet_tmp -e MYSQL_ROOT_PASSWORD=mysql -e MYSQL_USER=mysql -e MYSQL_PASSWORD=mysql -d kokosing/mysql

sleep 30

echo "create database autobet;" | docker exec -i mysql_autobet_tmp mysql -pmysql mysql
docker exec -i mysql_autobet mysqldump -pmysql autobet | \
  docker exec -i mysql_autobet_tmp mysql -pmysql autobet

docker commit mysql_autobet_tmp kokosing/mysql_autobet:latest
docker login -u="$DOCKER_LOGIN" -p="$DOCKER_PASSWORD"
docker push kokosing/mysql_autobet:latest
