#!/bin/bash

if javac Main.java; then
    java Main training.txt testing.txt
fi
