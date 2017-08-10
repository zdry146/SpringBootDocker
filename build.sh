#!/bin/bash

source /etc/profile

MYIMAGE=192.168.2.100:5000/springboot/springboot-jpa

# uncomment if you need push
#docker login 192.168.1.2:8082 -u admin -p admin123

containerName=springboot-jpa
x=`docker ps -a | grep "$containerName" | wc -l`

if [ $x = 1 ] ; then
# stop all container
    y=`docker ps  | grep "$containerName" | wc -l`

    if [ $y = 1 ] ; then
        docker kill $containerName
    fi

# remove all container
    docker rm $containerName
fi

# remove old images

dockerImage=`docker images | grep $MYIMAGE | awk '{print $3}'`

if [ "$dockerImage"  ] ; then
    docker rmi $dockerImage
fi

# build jar and image
mvn package -e -X docker:build -DskipTest

# running container
docker run -dp 8080:8080 --name $containerName ${MYIMAGE}

# push image
docker push ${MYIMAGE}


