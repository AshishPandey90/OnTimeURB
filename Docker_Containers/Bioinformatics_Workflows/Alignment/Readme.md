

This folder contains details to enable Alignment workflow on private machines using docker. Below are the steps to be followed to host the docker container.

# Alignment-Workflow
Below figure shows the pipeline of the workflow:

![](Images/Alignment_wf.PNG)


Alignment workflow is used to align NGS data or RNA-Seq reads to reference genome. The workflow starts with building the Bowtie index from a set of DNA sequences (FASTQ/FA) for a particular organism using Bowtie2. A set of 6 files with suffixes 'ebwt' are generated as outputs. The NGS reads will be aligned against the reference genome sequence using Tophat2 to identify exon-intron splice junctions. The outputs are in 'BAM' format files.

# Docker Container
The docker container is availbale on DockerHub and can be downloaded and initialized by below steps,

```
docker pull apfd6/alignment_wf  
docker run apfd6/alignment_wf  
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
Open .alignment-workflow.conf file and make below changes
```
[cyverse]
username = <your cyverse user name>
```

### Inputs to workflow
**inputs-fastq.txt**

URLs are given in the **irods:///[path]/[filename]** format. 

For example, to specify file **/iplant/home/zl7w2/readsleft.fq** use:
```
irods:///iplant/home/zl7w2/readsleft.fq
```
Do not use comments or whitespace in the file. Make sure you have the permission of the data, you could check from the [https://de.cyverse.org/de/](https://de.cyverse.org/de/)

**inputs-ref.txt**

Reference genome should be in fasta format. For example, 
```
irods:///iplant/home/zl7w2/Gmax_275_v2.0.ch1.fa
```
**inputs-gtf.txt**

Reference genome should be in fasta format. For example, 
```
irods:///iplant/home/zl7w2/Gmax_275_Wm82.a2.v1.gene.gtf
```
**main.conf**

Specify your input data type (pair or single) and output folder in main.conf as below:
```
#single-end or paired-end
inputs-style = paired-end
output_dir = /iplant/home/zl7w2/output
```

### Outputs of workflow

Workflow generates index reference genome and binary alignment map (BAM) files.


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

