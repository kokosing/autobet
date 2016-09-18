#!/bin/bash -ex

echo "Starting previously created devenv for autobet"

docker rm -f mysql_autobet || true

docker pull kokosing/mysql_autobet

docker run --name mysql_autobet -p 13306:3306 -d kokosing/mysql_autobet
