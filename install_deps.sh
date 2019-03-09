#!/bin/bash
rm -rf lib/*
mkdir -p lib
wget -O lib/fastjson-1.2.45.jar http://search.maven.org/remotecontent?filepath=com/alibaba/fastjson/1.2.45/fastjson-1.2.45.jar
git clone https://github.com/Z3Prover/z3.git
cd z3
python scripts/mk_make.py --java
cd build
make examples
ls
cp ./com.microsoft.z3.jar ../../lib
make
make install