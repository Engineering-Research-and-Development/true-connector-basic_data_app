#!/bin/bash

echo "Installing maven dependencies..."

mkdir -p $HOME/.m2/repository/de

cp -rf ./ci/.m2/de/*  $HOME/.m2/repository/de/
