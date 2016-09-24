#!/bin/bash -ex

if ! docker ps | grep mysql_autobet -q; then
  source bin/devenv_start.sh
  sleep 20
fi

java -jar target/autobet-0.1-SNAPSHOT-executable.jar stats -t PT10M
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -t PT1M -s random
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -c PT1M -s goal_based
