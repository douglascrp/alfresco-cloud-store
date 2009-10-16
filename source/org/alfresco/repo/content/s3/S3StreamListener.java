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
import java.io.FileInputStream;
import java.io.File;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

//S3 Imports

import org.jets3t.service.Constants;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

public class S3StreamListener implements ContentStreamListener {

	S3Service s3;
	S3Bucket bucket;

	private S3ContentWriter writer;
	
	private static final Log logger = LogFactory.getLog(S3StreamListener.class);

	public S3StreamListener(S3ContentWriter writer) {
		this.writer = writer;
		this.s3 = writer.getS3Service();
		this.bucket = writer.getBucket();
	}

	public void contentStreamClosed() throws ContentIOException {
		
		logger.debug("S3StreamListener.contentStreamClosed(): Retrieving Temp File Stream");
		try {
			//byte[] content = FileCopyUtils.copyToByteArray(writer.getTempFile());
			
			File file = writer.getTempFile();
			
			long size = file.length();
			
			writer.setSize(size);

			String url = writer.getNodeUrl();
			
			S3Object object = new S3Object(url);
			object.setDataInputFile(file);
			object.setContentLength(size);
			object.setContentType("application/octetstream");						
			
			try {
				
				s3.putObject(bucket, object);

			} catch (S3ServiceException e) {
				logger.error("S3StreamListener Failed to Upload File: " + e.getMessage());
				e.printStackTrace();
			} finally {
				object.closeDataInputStream();
			} // end try-catch-finally

		} catch (Throwable t) {
			t.printStackTrace();
		}// end try-catch
	}// end contentStreamClosed()

} // end class S3StreamListener