This folder contains details to enable RNASeq workflow on private machines using docker. Below are the steps to be followed to host the docker container.

#RNASeq-Workflow
Below figure shows the pipeline of the workflow:

![](Images/rnaseq_wf.PNG)

The docker container is availbale on DockerHub and can be downloaded and initialized by below steps,

docker pull apfd6/rnaseq_wf
docker run apfd6/rnaseq_wf
docker exec --user bamboo -it <ContainerId> bash

#Configuring the container

Create User Credentials:

$ mkdir -p ~/.ssh
$ ssh-keygen -t rsa -b 2048 -f ~/.ssh/workflow
  (just hit enter when asked for a passphrase)
$ cat ~/.ssh/workflow.pub >>~/.ssh/authorized_keys

