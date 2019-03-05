#!/bin/bash
rm -rf lib/*
mkdir -p lib
curl -o lib/fastjson-1.2.45.jar http://search.maven.org/remotecontent?filepath=com/alibaba/fastjson/1.2.45/fastjson-1.2.45.jar
curl -o lib/org.sat4j.pb-2.0.0-20081006.jar http://search.maven.org/remotecontent?filepath=org/sat4j/pb-2.0.0-20081006/org.sat4j.pb-2.0.0-20081006.jar

