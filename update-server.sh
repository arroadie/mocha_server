#!/usr/bin/env bash
ssh geekon32.snc1 <<'ENDSSH'
cd /var/groupon/deadpool/deadpool_server
git remote update && git rebase
sbt clean assembly
pgrep java  | xargs kill
nohup java -jar /var/groupon/deadpool/deadpool_server/target/scala-2.11/deadpool_server-assembly-1.0.jar &
ENDSSH
