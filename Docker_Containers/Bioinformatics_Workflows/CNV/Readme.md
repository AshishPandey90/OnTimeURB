
This folder contains details to enable CNV workflow on private machines using docker. Below are the steps to be followed to host the docker container.

# CNV-Workflow
Below figure shows the pipeline of the workflow:

![](Images/cnv_wf.PNG)

Copy number variation (CNVs) workflow is used to perform efficient analysis to detect CNVs in the form of gains and losses, from NGS reads. CNV workflow requires user to input a reference sequence and one or multiple sample/condition sequences which should be in BAM format. First, Samtools will extract the map locations from each mapped sequence reads. Outputs are generated in a hits format file which is required for next step as the input of CNV-Seq. The CNVs will be detected also with the confidence values selected by user.

# Docker Container
The docker container is availbale on DockerHub and can be downloaded and initialized by below steps,

```
docker pull apfd6/cnv_wf  
docker run apfd6/cnv_wf  
docker exec --user bamboo -it <ContainerId> bash  

(move to home folder i.e. /home/bamboo)  
cd ..  
```

## Configuring the container

### Initialize HTCondor

```
cd condor-8.8.9
. ./condor.sh
condor_master
```

### User Credentials:
The workflow requires 2 different user credentials:  a workflow ssh key to access data on the submit host and user’s iPlant password to access the data in iRods.  

#### Workflow SSH Key  
```
$ mkdir -p ~/.ssh  

$ ssh-keygen -t rsa -b 2048 -f ~/.ssh/workflow  
  (just hit enter when asked for a passphrase)  
  
$ cat ~/.ssh/workflow.pub >>~/.ssh/authorized_keys
```

#### iPlant connection file

To access data from the iPlant iRods repository, you need a file in your home directory. The name and format of this file depends on if you are using a system with iRods version 3 or version 4. For version 3, you need a file named ~/irods.iplant.json, with 0600 permission and content as below:
```
{
    "irods_host": "data.iplantcollaborative.org",  
    "irods_port": 1247,  
    "irods_user_name": "YOUR_IRODS_USERNAME",  
    "irods_zone_name": "iplant",  
    "irodspassword": "YOUR_IRODS_PASSWORD"  
}

$ chmod 0600 irods.iplant.json
```
#### Initialize workflow configuration file
Open .cnv-workflow.conf file and make below changes
```
[cyverse]
username = <your cyverse user name>
```

### Inputs to workflow
**inputs-bam.txt**

The list of bam file need to be compared to the reference bam file.
URLs are given in the **irods:///[path]/[filename]** format. 

For example, to specify file **/iplant/home/zl7w2/ERR986083_sorted_reads.bam** use:
```
irods:///iplant/home/zl7w2/ERR986083_sorted_reads.bam
```
Do not use comments or whitespace in the file. Make sure you have the permission of the data, you could check from the [https://de.cyverse.org/de/](https://de.cyverse.org/de/)

**inputs-ref.txt**

Specify the reference bam file that each bam file in the **inputs-bam.txt** compared with.
```
irods:///iplant/home/zl7w2/ERR986082_sorted_reads.bam
```

**main.conf**
Specify the output folder and p-value, log2 criteria
```
output_dir = /iplant/home/zl7w2/output
# configuration
log2 = 0.6
pvalue = 0.001
```

### Outputs of workflow
- Map locations .hits file
- Count value of each map location for test and ref case  .count file
- CNV with log2 fold change and p-value .cnv file

### Initialize Workflow
```
cd rnaseq
./workflow-generator --exec-env distributed
```

### Running and Monitoring the workflow

Note that when Pegasus plans/submits a workflow, a work directory is created and presented in the output. This directory is the handle to the workflow instance and used by Pegasus command line tools. The first tool to use is pegasus-run, which will start the workflow:  

pegasus-run [wfdir]  
Some useful tools to know about:  

pegasus-status -v [wfdir] - Provides status on a currently running workflow.  
pegasus-analyzer [wfdir] - Provides debugging clues why a workflow failed. Run this after a workflow has failed.  
pegasus-statistics [wfdir] - Provides statistics, such as walltimes, on a workflow after it has completed.  
pegasus-remove [wfdir] - Removes a workflow from the system.  

