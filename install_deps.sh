#!/bin/bash
rm -rf lib/*
mkdir -p lib
wget -O lib/fastjson-1.2.45.jar http://search.maven.org/remotecontent?filepath=com/alibaba/fastjson/1.2.45/fastjson-1.2.45.jar
apt-get update -qq -y
apt-get install binutils g++ make ant -y
apt-get clean
rm -rf /var/lib/apt/lists/*
Z3_DIR="$(mktemp -d)"
cd "$Z3_DIR"
wget -qO- https://github.com/Z3Prover/z3/archive/z3-${Z3_VERSION}.tar.gz | tar xz --strip-components=1
python scripts/mk_make.py --java
cd build
make
make install
cd /
rm -rf "$Z3_DIR"