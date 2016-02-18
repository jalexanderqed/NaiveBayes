#!/bin/bash

if make; then
    java src.Main training.txt testing.txt
fi
