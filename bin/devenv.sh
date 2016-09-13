#!/bin/bash -ex

docker rm -f mysql_autobet || true

docker run --name mysql_autobet -p 13306:3306 -d kokosing/mysql_autobet

