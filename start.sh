#!/bin/bash

make clean;

if make; then
    java DecisionTreeClassifier training.txt testing.txt
fi
