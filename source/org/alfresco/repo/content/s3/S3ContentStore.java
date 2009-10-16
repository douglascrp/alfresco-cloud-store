/*
 * Copyright (C) 2009 Alfresco Software Limited.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.alfresco.repo.content.s3;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//S3 Imports

import org.jets3t.service.Constants;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

/**
 * Amazon S3 Content Store Implementation
 * {@link org.alfresco.repo.content.ContentStore}.
 * 
 * @author Luis Sala
 */
public class S3ContentStore extends AbstractContentStore {
	
	private String accessKey;
	private String secretKey;
	private String bucketName;

	private S3Service s3;
	private S3Bucket bucket;
	
	private static final Log logger = LogFactory.getLog(S3ContentStore.class);

	/**
	 * Initialize an S3 Content Store.
	 * 
	 * @param accessKey
	 *            Amazon Web Services Access Key
	 * @param secretKey
	 *            Amazon Web Services Secret Key 
	 * @param bucketName
	 *            Name of S3 bucket to store content into.
	 */
	public S3ContentStore(String accessKey, String secretKey, String bucketName) {

		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.bucketName = bucketName;
		
		logger.info("S3ContentStore Initializing: accessKey="+accessKey+" secretKey="+secretKey+" bucketName="+bucketName);
		// Instantiate S3 Service and create necessary bucket.
		try {
			s3=new RestS3Service(new AWSCredentials (accessKey, secretKey));
			logger.info("S3ContentStore Creating Bucket: bucketName="+bucketName);
			bucket = s3.createBucket(bucketName);
			logger.info("S3ContentStore Initialization Complete");
		} catch (S3ServiceException se) {
			logger.error("S3ContentStore Initialization Error in Constructor: "+ se.toString());
		} // end try-catch
		
	} // end constructor


	public ContentReader getReader(String contentUrl) throws ContentIOException {
		try {
			return new S3ContentReader(contentUrl, s3, bucket);
		} catch (Throwable e) {
			throw new ContentIOException("S3ContentStore Failed to get reader for URL: " + contentUrl, e);
		}
	}

	public ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl)
			throws ContentIOException {
		try {
            String contentUrl = null;
            // Was a URL provided?
            if (newContentUrl == null || newContentUrl == "") {
            	contentUrl = createNewUrl();
            } else {
            	contentUrl = newContentUrl;
            }

			return new S3ContentWriter(contentUrl, existingContentReader, s3, bucket);
		} catch (Throwable e) {
			throw new ContentIOException("S3ContentStore.getWriterInternal(): Failed to get writer.");
		}
	}

	public boolean delete(String contentUrl) throws ContentIOException {

		try {
			logger.debug("S3ContentStore Deleting Object: contentUrl="+contentUrl);
			s3.deleteObject(bucket, contentUrl);
			return true;
		} catch (S3ServiceException e) {
			logger.error("S3ContentStore Delete Operation Failed: " + e.getMessage());
			e.printStackTrace();
		} finally {
			cleanup();
		} // end try-catch-finally

		return false;
	} // end delete
	
	// Intended to reset connections, buckets, etc. at some point in the future.
	public void cleanup() {
		// TODO Implement any necessary cleanup.
	} // end cleanup
	
	public boolean isWriteSupported() {
		// TODO Auto-generated method stub
		return true;
	}	
/*
	public Set<String> getUrls(Date createdAfter, Date createdBefore) throws ContentIOException {
		// TODO There is a S3Service.getObject(...) method that may support this. 
		return null;
	}
*/
	
    /**
     * Creates a new content URL.  This must be supported by all
     * stores that are compatible with Alfresco.
     * 
     * @return Returns a new and unique content URL
     */
	public static String createNewUrl() {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        sb.append(FileContentStore.STORE_PROTOCOL)
          .append(ContentStore.PROTOCOL_DELIMITER)
          .append(year).append('/')
          .append(month).append('/')
          .append(day).append('/')
          .append(hour).append('/')
          .append(minute).append('/')
          .append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;

	} // end createNewUrl
	
	public String getRelativePath(String contentUrl) {
		// take just the part after the protocol
        Pair<String, String> urlParts = super.getContentUrlParts(contentUrl);
        String protocol = urlParts.getFirst();
        String relativePath = urlParts.getSecond();
        
        return relativePath;
	}
} // end class S3ContentStore