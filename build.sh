#!/bin/bash

source /etc/profile

MYIMAGE=192.168.2.8:5000/springboot/springboot-jpa

# uncomment if you need push
#docker login 192.168.1.2:8082 -u admin -p admin123

# stop all container
docker kill springboot-jpa

# remove all container
docker rm springboot-jpa

# remove old images
docker images | grep 192.168.2.8:5000/springboot/springboot-jpa | awk '{print $3}' | xargs docker rmi

# build jar and image
mvn package -e -X docker:build -DskipTest

# running container
docker run -dp 8080:8080 --name springboot-jpa ${MYIMAGE}

# push image
#docker push ${MYIMAGE}


