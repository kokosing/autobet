#!/bin/bash -ex

if ! docker ps | grep mysql_autobet -q; then
  source bin/devenv_start.sh
  sleep 20
fi

java -jar target/autobet-0.1-SNAPSHOT-executable.jar stats -t PT10M
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -t PT1M -s random
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -t PT1M -s low_bet
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -t PT1M -s chances_based_on_goal_based_rater
