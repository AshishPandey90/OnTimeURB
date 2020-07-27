# OnTimeURB Container

# Docker Container
The docker container is availbale on DockerHub and can be downloaded and initialized by below steps,
```
docker pull apfd6/ontimeurb
docker run -it apfd6/ontimeurb bash

```

## Configuring OnTimeURB


## Starting OnTimeURB

```
docker ps

# This will fetch the container id
docker exec --user root -it <container_isd> bash

#Move to base home folder
cd

#Move to apache and start the ontimeurb service
cd apache/bin
./startup.sh

#exit the container console
Ctrl + p + q

# Attach docker port to host port for the image
docker run -d --name myapp -p 8080:80 myappimage

#get ip address for container
docker inspect <container_id>

# The we service will initiate at
http://<ip_address>:8080/OnTimeURB/getTemplateCatalog

```

### Initialize Workflow


### Running and Monitoring the workflow

