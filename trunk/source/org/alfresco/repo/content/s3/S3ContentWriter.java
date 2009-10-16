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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.GUID;
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

public class S3ContentWriter extends AbstractContentWriter {

	private String nodeUrl;

	private String uuid;

	private File tempFile;
	
	private long size;
	
	private S3Service s3;
	
	private S3Bucket bucket;
	
	private static final Log logger = LogFactory.getLog(S3ContentWriter.class);
	
	public S3ContentWriter(String nodeUrl, ContentReader existingContentReader, S3Service s3, S3Bucket bucket) {
		super(nodeUrl, existingContentReader);
		this.nodeUrl = nodeUrl;
		this.s3=s3;
		this.bucket=bucket;
		this.uuid=GUID.generate();
		addListener(new S3StreamListener(this));
	}

	@Override
	protected ContentReader createReader() throws ContentIOException {
		return new S3ContentReader(getContentUrl(), s3, bucket);
	}

	@Override
	protected WritableByteChannel getDirectWritableChannel() throws ContentIOException {
        try
        {
        	logger.debug("S3ContentWriter Creating Temp File: uuid="+uuid);
    		tempFile = TempFileProvider.createTempFile(uuid, ".bin");
            OutputStream os = new FileOutputStream(tempFile);
            logger.debug("S3ContentWriter Returning Channel to Temp File: uuid="+uuid);
            return Channels.newChannel(os);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("S3ContentWriter.getDirectWritableChannel(): Failed to open channel. " + this, e);
        }
	}

	@Override
	public ContentData getContentData() {
		ContentData property = new ContentData(getNodeUrl(), getMimetype(), getSize(),
				getEncoding());
		return property;
	}

	// GETTERS AND SETTERS
	/*
	public String getContentUrl() {
		if (uuid == null) {
			return nodeUrl;
		} else {
			return nodeUrl;
		}
	}
	*/

	public String getNodeUrl() {
		return nodeUrl;
	}

	public void setNodeUrl(String nodeUrl) {
		this.nodeUrl = nodeUrl;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}


	public File getTempFile() {
		return tempFile;
	}

	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}

	public S3Service getS3Service() {
		return s3;
	}
	
	public S3Bucket getBucket() {
		return bucket;
	}

} // end class S3ContentWriter