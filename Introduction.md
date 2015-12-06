# Introduction #

This content store implementation uses the jets3t API for Amazon S3 store content into S3 instead of the local FileSystem.


# Development #

Simply add this project as a new project to an existing Eclipse Workspace that's already configured with either the Alfresco SDK or an Alfresco SVN checkout.

# Building #

You must edit the following lines on build.xml to point to your Alfresco installation and the "root" directory of your checked out source code:
```
<property name="alfresco.install.dir" value="/PATH/TO/ALFRESCO/INSTALL"/>
<property name="alfresco.src.dir" value="/PATH/TO/ALFRESCO/SOURCE/OR/SDK"/>
```
For example:
```
<property name="alfresco.install.dir" value="/Users/joebloggs/Alfresco"/>
<property name="alfresco.src.dir" value="/Users/joebloggs/AlfrescoSource/HEAD/root"/>
```
The build script has been tested only from within Eclipse.

# Installation #

The default configuration will work automatically. Simply build an AMP and deploy it with the Module Management Tool as is standard.

You will need to edit custom-repository.properties and supply your Amazon AWS credentials plus an S3 bucket name.

eg.
```
s3.accesskey=12345ABCD1234ABCD
s3.secretkey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
s3.bucketname=alfs3bucketsample
```

# Alternate Configuration #

One can configure a replicated configuration that uses both the local filesystem and S3 to store content. The primary benefit is that the local filesystem can be purged at will in order to control disk utilization. For example, a cron job could be used to clear the local filesystem contentstore every night at midnight. This can happen while Alfresco is still running. Request for files will be redirected to S3 which will then populate the local filestore to make future read operations perform faster.

See: [replicating-s3-content-services-context.xml.sample](http://code.google.com/p/alfresco-cloud-store/source/browse/trunk/replicating-s3-content-services-context.xml.sample)

# Contributing #

Contributing to the project requires signing a Contribution Agreement. Please contact the project administrator for instructions.