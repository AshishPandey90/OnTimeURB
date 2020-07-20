
This folder contains details to enable Methylation workflow on private machines using docker. Below are the steps to be followed to host the docker container.

# Methylation-Workflow
Below figure shows the pipeline of the workflow:

![](Images/methylation_wf.PNG)


DNA methylation is one of the main epigenetic modifications in the genome. We have developed the methylation workflow which meets variety of user analysis demands in an efficient and integrative manner. This workflow starts with raw sequence data (FASTQ files).   
The input data for methylation workflow consists of high-throughput NGS bisulfite sequencing reads either single-end or paired-end. The reads are aligned back to the reference genome via BSMAP. The program To-mr is used to convert the output generated from the mapper to the MR format used in the next step. Before calculating methylation level, read duplicates are removed via Duplicate-remover. The methylation level for every cytosine site at single base resolution is estimated via Methcounts as a probability based on the ratio of methylated to total reads mapped to that loci. For some bisulfite sequencing project with multiple replicates, Merge-methcount will be used to merge those individual methcounts file to produce a single estimate that has higher coverage. Since symmetric methylation level is common for CpG methylation, the workflow will extract and merge symmetric GpG methylation levels via Symmetric-cpgs tool.   
Various other methylation analysis results will be produced such as hypo-methylated regions (HMRs), hyper-methylated regions (HyperMR) and differentially methylated regions (DMR) between two methylomes. Hmr and Hypermr are used to identify the HMRs and hypermethlated regions. Finally, the DM regions between a pair or two small groups of methylomes will be identified via Methdiff and Dmr.   

# Docker Container
The docker container is availbale on DockerHub and can be downloaded and initialized by below steps,

```
docker pull apfd6/methylation_wf  
docker run apfd6/methylation_wf  
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
Open .methylation-workflow.conf file and make below changes
```
[cyverse]
username = <your cyverse user name>
```

### Inputs to workflow

### Outputs of workflow

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

