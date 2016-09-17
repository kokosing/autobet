#!/bin/bash -ex

source bin/devenv_start.sh

sleep 20

java -jar target/autobet-0.1-SNAPSHOT-executable.jar stats -c 5000
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -c 500 -s random
java -jar target/autobet-0.1-SNAPSHOT-executable.jar eval -c 500 -s goal_based
