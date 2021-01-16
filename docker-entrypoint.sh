#!/usr/bin/env bash
set -e
case $(echo $1 | awk -F ' ' '{print $1}') in
  start)
    jarName=$(ls | grep -E .+\.jar$)
    java $JVM_ARGS -jar -DAppPID $jarName
  ;;

  *)
    $*
  ;;
esac
