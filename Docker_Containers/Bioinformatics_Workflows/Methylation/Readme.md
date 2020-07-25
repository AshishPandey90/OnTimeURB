
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
**main.conf**

Specify your input data type (paired or single) and output folder in main.conf as below
```
#single-end or paired-end
inputs-style = paired-end
output_dir = /iplant/home/zl7w2/output
```

Samples should be list as format sample_# with it fastq files counts, for example, if you have four samples and each of the sample has 4, 6, 8, 2 fastq files. It shoulbe be edit in the main.conf as below:
```
#the replica name of each sample
sample_0 = 4
sample_1 = 6
sample_2 = 8
sample_3 = 2
```
The order of the samples should be consist with it's order in the **inputs-fastq.txt** file. 

Specify the comparison of samples to calculate the differential expression, for example, if you want to compare sample_0 with sample_1, sample_1 with sample_2 and sample_0 with sample_3:
```
#choose the samples which need to be compared
sample_compare_1 = sample__0 & sample__1
sample_compare_2 = sample__1 & sample__2
sample_compare_3 = sample__0 & sample__3
```
### Outputs of workflow
Workflow generates 
- index reference genome 
- binary alignment map (BAM) files 
- mapped file .mr format
- methylation level .methcount
- symmetric-cpgs methylation level
- hypo-methylated regions (HMRs)
- hyper-methylated regions (HyperMRs)
- differential methylated regions (DMRs)



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

