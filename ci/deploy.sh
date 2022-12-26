#!/bin/bash

echo "Creating Docker Container from Data-App repo..."
sudo docker build -f Dockerfile -t rdlabengpa/ids_be_data_app:develop .
cd ..
echo "Data-App is ready"
echo "Starting deployment to Docker Hub"
sudo docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD}
sudo docker push rdlabengpa/ids_be_data_app:develop
echo "Data-App deployed to Docker Hub"