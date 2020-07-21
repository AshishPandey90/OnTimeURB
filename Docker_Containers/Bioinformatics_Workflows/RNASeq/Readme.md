This folder contains details to enable RNASeq workflow on private machines using docker. Below are the steps to be followed to host the docker container.

# RNASeq-Workflow
Below figure shows the pipeline of the workflow:

![](Images/rnaseq_wf.PNG)


RNA-Seq analysis workflow is used to perform quantification of gene expression from RNA-Seq transcriptomics data and statistical analysis to discover differential expressed genes/isoform between various experimental groups conditions. The paired-end or single-end reads are aligned to the reference genome via Tophat2. The mapped reads are summarized and aggregated over genes and isoforms for a particular organism’s gene and genome version to then calculate the gene expression FPKMs values via Cufflinks. Then, the transcriptome assembly generated from Cufflinks will be processed via Cuffcompare to perform these comparisons and assess the quality of assembly. Finally, genes and isoforms expressed differentially between the various pair wise comparisons within experimental groups/conditions are identified using Cuffdiff.

# Docker Container
The docker container is availbale on DockerHub and can be downloaded and initialized by below steps,

```
docker pull apfd6/rnaseq_wf  
docker run apfd6/rnaseq_wf  
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
Open .rnaseq-workflow.conf file and make below changes
```
[cyverse]
username = <your cyverse user name>
```

### Inputs to workflow
**inputs-fastq.txt**

URLs are given in the **irods:///[path]/[filename]** format. 

For example, to specify file **/iplant/home/shared/digbio/SoyKB/PGen_workflow/SRR1209394.fastq.gz** use:
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
Workflow generates 
- index reference genome **ref.tar**
- binary alignment map (BAM) files **tophat_c*_r*.tar**
- cufflinks results - FPKM **cufflinks_c*_r*.tar**
- cuffcompare results - transcript file **cuffcompare_s*_s*.combined.gtf**
- cuffdiff results - differntial expression **cuffdiff_s*s***
  - cds.count_tracking
  - cds.fpkm_tracking
  - cds.read_group_tracking
  - cds_exp.diff
  - genes.count_tracking
  - genes.fpkm_tracking
  - genes.read_group_tracking
  - gene_exp.diff
  - isoforms.count_tracking
  - isoforms.fpkm_tracking
  - isoforms.read_group_tracking
  - isoform_exp.diff
  - tss.count_tracking
  - tss.fpkm_tracking
  - tss.read_group_tracking
  - tss_exp.diff
  
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

