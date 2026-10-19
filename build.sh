#!/bin/sh
mkdir -p out && javac -encoding UTF-8 -d out $(find src test -name "*.java")
