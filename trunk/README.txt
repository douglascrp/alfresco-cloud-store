S3 Content Store for Alfresco
=============================

This content store implementation uses the jets3t API for Amazon S3 store content into S3 instead of the local FileSystem.

Development:
============

Simply add this project as a new project to an existing Eclipse Workspace that's already configured with either the Alfresco SDK or an Alfresco SVN checkout.

Building:
=========
You must edit the following lines on build.xml to point to your Alfresco installation and the "root" directory of your checked out source code:

<property name="alfresco.install.dir" value="/PATH/TO/ALFRESCO/INSTALL"/>
<property name="alfresco.src.dir" value="/PATH/TO/ALFRESCO/SOURCE/OR/SDK"/>

You may also need to change the following values if you are using the Alfresco SDK rather than source code checkout of Alfresco.
<property name="alfresco.lib.dir" value="${alfresco.src.dir}/projects/web-client/build/assemble/WEB-INF/lib"/>
<property name="war.file" value="../../../HEAD/root/projects/web-client/build/dist/alfresco.war"/>

For example:

<property name="alfresco.install.dir" value="/Users/joebloggs/Alfresco"/>
<property name="alfresco.src.dir" value="/Users/joebloggs/AlfrescoSource/HEAD/root"/>

<property name="alfresco.lib.dir" value="${alfresco.src.dir}/lib"/>
<property name="war.file" value="${alfresco.deploy.dir}/alfresco.war"/>

The build script has been tested only from within Eclipse.

Installation:
=============

The default configuration will work automatically. Simply build an AMP and deploy it with the Module Management Tool as is standard.

You will need to edit alfresco-global.properties and supply your Amazon AWS credentials plus an S3 bucket name.

eg.

s3.accesskey=12345ABCD1234ABCD
s3.secretkey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
s3.bucketname=alfs3bucketsample

The project is configured to use a caching content store. The following properties will need to be added to your alfresco-global.properties.

dir.cachedcontent=/path/to/cache
system.content.caching.cacheOnInbound=true
system.content.caching.maxDeleteWatchCount=1
system.content.caching.contentCleanup.cronExpression=0 0 3 * * ?
system.content.caching.timeToLiveSeconds=0 
system.content.caching.timeToIdleSeconds=60
system.content.caching.maxElementsInMemory=5000
system.content.caching.maxElementsOnDisk=10000
system.content.caching.minFileAgeInMillis=2000
system.content.caching.maxUsageMB=4096
system.content.caching.maxFileSizeMB=0

For more information on these properties and caching content store configuration see http://wiki.alfresco.com/wiki/CachingContentStore.

Alternate Configuration:
========================

One can configure a replicated configuration that uses both the local filesystem and S3 to store content. The primary benefit is that the local filesystem can be purged at will in order to control disk utilization. For example, a cron job could be used to clear the local filesystem contentstore every night at midnight. This can happen while Alfresco is still running. Request for files will be redirected to S3 which will then populate the local filestore to make future read operations perform faster.

See: replicating-s3-content-services-context.xml.sample

Contributing
============

Contributing to the project requires signing a Contribution Agreement. Please contact the project administrator for instructions.
