# OnTimeURB
Grauate Research Project

OnTimeURB stands for on time universal broker.
This framework is created for configuring cloud infrastructure for scientfic/bioinformatics workflows. There are three components to this infrastructure deployment.
a) Configuring the infratructure with tools which help in distributed computing.
b) Optimizer engine which helps in resource and service brokering for the users.
c) Machine learning model to learn about performance variation incurred with execution of workflows on the infrastructure.
d) Workflows are not included in this repository, but can be referered at https://github.com/pegasus-isi/PGen-GenomicVariations-Workflow


## Configuring the infrastructure
To create an infrastructure for distributed computing essentially a single submit server(host) and many client servers(clients) architecture is required. Towards that architecture varying configurations has to be made in submit host and clients. Below tools are used for ceating the infrastructure,

a) A linux host machine with good resource configuration to host the cluster of clients interaction. A machine with 2-4 CPU cores, 8-16 GB RAM, 500 GB storage can be recommended, but it is subjective to user.
b) Pegasus Workflow Management System
c) HTCondor high throughput computing
d) pyglidein (python server-client pair for submitting HTCondor glidein jobs on remote batch systems)

Steps to be followed for configuration,
### On Submit Host Server
A.	Install HTCondor personal edition
  --Download HTCondor 8.8.4 from: https://research.cs.wisc.edu/htcondor/downloads/
  --Install following the page https://research.cs.wisc.edu/htcondor/instructions/ubuntu/18/stable/
Need to run this command before beginning: (sudo chmod 777 /etc/apt/sources.list)

B.	Install Pegasus
  --Install without root access or follow the strategy above for HTCondor
    https://pegasus.isi.edu/downloads/
    
C.	Install PyGlidein
      https://pegasus.isi.edu/documentation/pyglidein.php
    To setup password for HTCondor, follow this page:
      https://research.cs.wisc.edu/htcondor/manual/v7.8/3_6Security.html
    specifically, the command to generate a password file is:
      condor_store_cred -f file (or with path to file)
      
D.	Configure condor_config file in the /condor/etc/ folder to the working template, included is the command to configure the port to be static so we do not have to change it every time we restart the server. Then use the command “        ” to open up the port.

E.	Configure sites.xml file in the workflow work dir using the working template with the scp commanded added and local storage/scratch folder paths.

F.	Start a new job, wait for it to become Idle forever, then start the server 

G.	After starting the client, check condor_q and condor_status to check for the attached Lewis computing nodes, we can see a new computing node will be added and stays busy for a while

H.	When the job status goes to done, check the output folder for job results. 

### On Client Server

A.	Install PyGlidein following the website: https://pegasus.isi.edu/documentation/pyglidein.php

B.	If there is no job scheduler on the client, we need to install HTCondor by compiling the tarball (this could be very difficult and easily fail)
    If you have a scheduler already installed on the client node, then there is no need to generate a new tarball on the client, just       download the example one from the website, untar it, replace the password file by downloading/uploading the password file generated     on the server (Step 1), then chmod the password file to 400, and make the tarball again

C.	On Lewis account, create and configure a lewis.config file to configure the running environment and connection to the submit host

D.	On Lewis node, create the two folders specified in the server config file (condor_config) to hold temp data

E.	Create a new folder (e.g. glidein) for holding glidein and log files on the node, it looks like the default scratch on Lewis is not     compatible with glidein (according to Mats)

F.	Modify the glidein_start.sh script to use the collector port that we created on the server (lines 86-87), then comment out lines         180-220 and 233-234.

G.	After starting the server on the submit host, start the client to accept the job:
      $ pyglidein_client --config=lewis.config --secrets=secrets

H.	Go to glidein folder and then the job folder, check for log files especially the MasterLog

I.	Go to the out folder and check for job logs
