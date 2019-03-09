#!/bin/bash
CLASSPATH=classes:lib/*
JAVAS=$(find src -name '*.java')
mkdir -p classes
javac -cp $CLASSPATH -sourcepath src -d classes $JAVAS
