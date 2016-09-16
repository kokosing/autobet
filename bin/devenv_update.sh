#!/bin/bash -ex

source bin/devenv_start.sh

sleep 20

echo 'delete from key_value_store_entries;' | docker exec -i mysql_autobet mysql -pmysql autobet
java -jar target/autobet-0.1-SNAPSHOT-executable.jar stats -c 10000
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -c 1000 -s random
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -c 1000 -s goal_based
