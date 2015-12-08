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

package org.alfresco.repo.content.cloudstore;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

/**
 * Provides READ services against an S3 content store.
 * 
 * @author Luis Sala
 */
public class S3ContentReader extends AbstractContentReader {

	/**
	 * message key for missing content. Parameters are
	 * <ul>
	 * <li>{@link org.alfresco.service.cmr.repository.NodeRef NodeRef}</li>
	 * <li>{@link ContentReader ContentReader}</li>
	 * </ul>
	 */
	public static final String MSG_MISSING_CONTENT = "content.content_missing";

	private static final Log logger = LogFactory.getLog(S3ContentReader.class);

	private S3Object objectDetails;
	
	private String nodeUrl;

	private S3Service s3;
	
	private S3Bucket bucket;
	
	/**
	 * Constructor that builds a URL based on the absolute path of the file.
	 * 
	 * @param nodeUrl
	 *            url of the content node.
	 */
	public S3ContentReader(String nodeUrl, S3Service s3, S3Bucket bucket) {
		super(nodeUrl);
		this.nodeUrl = nodeUrl;
		this.s3 = s3;
		this.bucket = bucket;
		getDetails();
	}

	@Override
	protected ContentReader createReader() throws ContentIOException {
		logger.debug("S3ContentReader.createReader() invoked for contentUrl="+nodeUrl);
		return new S3ContentReader(nodeUrl, s3, bucket);
	}

	@Override
	protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException {
		try {
			// Confirm the requested object exists
			if (!exists()) {
				throw new ContentIOException("Content object does not exist");
			}
			logger.debug("S3ContentReader Obtaining Input Stream: nodeUrl="+nodeUrl);
			// Get the object and retrieve the input stream
			S3Object object = s3.getObject(bucket, nodeUrl);
			ReadableByteChannel channel = null;
			InputStream is = object.getDataInputStream();
			channel = Channels.newChannel(is);
			logger.debug("S3ContentReader Success Obtaining Input Stream: nodeUrl="+nodeUrl);
			return channel;
		} catch (Throwable e) {
			throw new ContentIOException("Failed to open channel: " + this, e);
		}
	} // end getDirectReadableByteChannel()

	public boolean exists() {
		return (objectDetails != null);
	} // end exists()

	public long getLastModified() {
		
		if (objectDetails == null) {
			return 0L;
		}
		
		return objectDetails.getLastModifiedDate().getTime();
	} // end getLastModified()

	public long getSize() {
		if (!exists()) {
			return 0L;
		}
		try {
			return objectDetails.getContentLength();
		} catch (Exception e) {
			return 0L;
		}
	}


	/**
	 * Gets information on a stream. Returns headers from the response.
	 * 
	 * @return Header[] array of http headers from the response
	 */
	private void getDetails() {
		if (objectDetails != null) {
			// Info already fetched, so don't do this again.
			return;
		}
		
		try {
			objectDetails = s3.getObject(bucket.getName(), nodeUrl);
		} catch (S3ServiceException e) {
			logger.warn("S3ContentReader Failed to get Object Details: " + e.getMessage());
			//e.printStackTrace();
		} catch (ServiceException e) {
            e.printStackTrace();
        } finally {
			cleanup();
		}
	} // end info()

	// Cleanup any necessary resources
	private void cleanup() {
		// TODO Perform any cleanup operations
	} // end cleanup

} // end class S3ContentReader
