# UPDATE #

This project was originally hosted at http://code.google.com/p/alfresco-cloud-store

As Alfresco now offers a supported S3 connector, this project was "dead", then I decided to export it to github before google code was turned off.

I've ported the project to use Alfresco SDK 2.1.1, and it is now built for Alfresco 5.0.d

# Introduction #

This content store implementation uses the jets3t API for Amazon S3 store content into S3 instead of the local FileSystem.

# Development #

As the project is now mavenized, there is no need to import it on Eclipse. All you need is Maven installed.

# Building #

In order to build the project, execute:

mvn clean install -DskipTests=true

You can even execute the project in order to test it executing the run.sh file, but first, you have to set your Amazon AWS credentials plus an S3 bucket name.
Edit the file src/test/properties/local/alfresco-global.properties and add the properties below:

eg.
```
s3.accesskey=12345ABCD1234ABCD
s3.secretkey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
s3.bucketname=alfs3bucketsample
```

# Installation #

The default configuration will work automatically. Simply build an AMP and deploy it with the Module Management Tool as is standard.

You will need to edit alfresco-global.properties and supply your Amazon AWS credentials plus an S3 bucket name.

eg.
```
s3.accesskey=12345ABCD1234ABCD
s3.secretkey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
s3.bucketname=alfs3bucketsample
```

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

# Alternate Configuration #

One can configure a replicated configuration that uses both the local filesystem and S3 to store content. The primary benefit is that the local filesystem can be purged at will in order to control disk utilization. For example, a cron job could be used to clear the local filesystem contentstore every night at midnight. This can happen while Alfresco is still running. Request for files will be redirected to S3 which will then populate the local filestore to make future read operations perform faster.

In order to enable this architecture, edit the service-context.xml and change the contentService bean from:

```
<bean id="contentService" parent="baseContentService">
    <property name="store">
        <!-- cached s3 contentStore
        <ref bean="cachingContentStore"/>
        -->
        <!-- localContentStore replicated to the cached s3 contentStore -->
        <ref bean="replicatedContentStore"/>
    </property>
</bean>
```

to
```
<bean id="contentService" parent="baseContentService">
    <property name="store">
        <!-- cached s3 contentStore -->
        <ref bean="cachingContentStore"/>

        <!-- localContentStore replicated to the cached s3 contentStore
        <ref bean="replicatedContentStore"/>
         -->
    </property>
</bean>
```

# Contributing #

Anyone is welcome to help implementing, documenting, testing or whatever you can do to help.