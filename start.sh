#!/bin/bash

if make; then
    java DecisionTreeClassifier training.txt testing.txt
fi
