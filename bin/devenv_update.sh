#!/bin/bash -ex

source bin/devenv_start.sh

sleep 20

java -jar target/autobet-0.1-SNAPSHOT-executable.jar stats -c 10000
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -c 1000 -s random
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -c 1000 -s goal_based
