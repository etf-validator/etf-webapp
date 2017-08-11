/**
 * Copyright 2010-2017 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.interactive_instruments.etf.webapp.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import de.interactive_instruments.Credentials;
import de.interactive_instruments.IFile;
import de.interactive_instruments.MimeTypeUtils;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dto.MetaDataItemDto;
import de.interactive_instruments.exceptions.IOsizeLimitExceededException;
import de.interactive_instruments.io.FileContentFilterHolder;
import de.interactive_instruments.io.MultiFileFilter;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class FileStorage {

	private final IFile storageDir;
	private final IFile tmpDir;
	private final FileContentFilterHolder baseFilter;
	private long maxStorageSize;
	private String maxStorageSizeHr;

	/**
	 * Create a new FileStorage
	 *
	 * The max storage size is defaulted to 10 GB.
	 *
	 * @param destination destination directory
	 * @param tmpDir temporary directory for up- and downloads
	 * @param baseFilter filter that is applied on up- and downloaded files
	 */
	FileStorage(final IFile destination, final IFile tmpDir, final FileContentFilterHolder baseFilter) {
		this.storageDir = destination;
		this.tmpDir = tmpDir;
		this.baseFilter = baseFilter;
		maxStorageSize = 10737418240L;
	}

	/**
	 * Change the max storage size
	 * @param maxStorageSize max storage size
	 */
	public void setMaxStorageSize(final long maxStorageSize) {
		this.maxStorageSize = maxStorageSize;
		this.maxStorageSizeHr = FileUtils.byteCountToDisplaySize(maxStorageSize);
	}

	abstract class StorageCmd {
		protected final String label;
		protected final MultiFileFilter fileFilter;

		StorageCmd(final String label, final FileFilter additionalFileFilter) {
			this.label = IFile.sanitize(label);
			// Apply additional filter
			if (additionalFileFilter == null) {
				fileFilter = baseFilter.filename();
			} else {
				fileFilter = baseFilter.filename().and(additionalFileFilter);
			}
		}
	}

	public class DownloadCmd extends StorageCmd {

		private final Collection<URI> uris;
		private final Credentials credentials;

		private DownloadCmd(final String label, final Collection<URI> uris, final FileFilter additionalFileFilter,
				final Credentials credentials) {
			super(label, additionalFileFilter);
			this.uris = uris;
			this.credentials = credentials;
		}

		IFile download() throws LocalizableApiError, IOException {
			// Create a temporary directory for the uploads
			final IFile tmpSubDir = tmpDir.secureExpandPathDown(label);
			tmpSubDir.ensureDir();

			// Destination directory
			final IFile destinationSubDir = storageDir.secureExpandPathDown(label);
			destinationSubDir.ensureDir();

			try {
				long remainingDownloadSize = maxStorageSize;
				for (final URI uri : uris) {
					if (!UriUtils.isFile(uri)) {
						final IFile download;
						try {
							download = UriUtils.downloadTo(uri, tmpSubDir, this.credentials, remainingDownloadSize);
							remainingDownloadSize -= download.length();
						} catch (IOException e) {
							try {
								tmpSubDir.deleteDirectory();
								destinationSubDir.deleteDirectory();
							} catch (IOException ignore) {}
							throw new LocalizableApiError("l.download.failed", false, 400, e, maxStorageSizeHr);
						}
						prepare(download, destinationSubDir, download.getName(), fileFilter, remainingDownloadSize);
					}
				}
				checkSize(destinationSubDir);
			} catch (IOsizeLimitExceededException e) {
				throw new LocalizableApiError("l.max.download.size.exceeded", false, 400, e, maxStorageSizeHr);
			}
			return destinationSubDir;
		}
	}

	public class UploadCmd extends StorageCmd {

		private final List<MultipartFile> files;

		UploadCmd(final String label, final Collection<List<MultipartFile>> multiFiles, final FileFilter additionalFileFilter) {
			super(label, additionalFileFilter);
			this.files = new ArrayList<>(multiFiles.size());
			for (final List<MultipartFile> files : multiFiles) {
				for (final MultipartFile file : files) {
					this.files.add(file);
				}
			}
		}

		IFile upload() throws LocalizableApiError {

			// Create a temporary directory for the uploads
			final IFile tmpSubDir = tmpDir.secureExpandPathDown(label);
			// Destination directory
			final IFile destinationSubDir = storageDir.secureExpandPathDown(label);

			try {
				tmpSubDir.ensureDir();
				destinationSubDir.ensureDir();

				uploadAndUnzip(files, fileFilter, tmpSubDir, destinationSubDir);
				checkSize(destinationSubDir);
			} catch (LocalizableApiError e) {
				try {
					tmpSubDir.deleteDirectory();
					destinationSubDir.deleteDirectory();
				} catch (IOException ignore) {}
				throw e;
			} catch (IOsizeLimitExceededException e) {
				try {
					tmpSubDir.deleteDirectory();
					destinationSubDir.deleteDirectory();
				} catch (IOException ignore) {}
				throw new LocalizableApiError("l.max.upload.size.exceeded", false, 400, e, maxStorageSizeHr);
			} catch (IOException e) {
				try {
					tmpSubDir.deleteDirectory();
					destinationSubDir.deleteDirectory();
				} catch (IOException ignore) {}
				throw new LocalizableApiError(e);
			}
			return destinationSubDir;
		}
	}

	private void checkSize(final IFile storageSubDir) throws IOsizeLimitExceededException {
		if (FileUtils.sizeOfDirectory(storageSubDir) > maxStorageSize) {
			throw new IOsizeLimitExceededException(maxStorageSize);
		}
	}

	DownloadCmd download(MetaDataItemDto dto, final FileFilter additionalFileFilter, final Credentials credentials,
			final Collection<URI> uris) {
		return new DownloadCmd(dto.getLabel() + "-" + dto.getId(), uris, additionalFileFilter, credentials);
	}

	DownloadCmd download(MetaDataItemDto dto, final FileFilter additionalFileFilter, final Credentials credentials,
			final URI... uris) {
		return new DownloadCmd(dto.getLabel() + "-" + dto.getId(), Arrays.asList(uris), additionalFileFilter, credentials);
	}

	UploadCmd upload(MetaDataItemDto dto, final FileFilter additionalFileFilter, final Collection<List<MultipartFile>> files) {
		return new UploadCmd(dto.getId().getId(), files, additionalFileFilter);
	}

	private void uploadAndUnzip(final Collection<MultipartFile> files, final MultiFileFilter fileFilter, final IFile tmpSubDir,
			final IFile destinationSubDir) throws LocalizableApiError, IOException {
		long remainingDownloadSize = maxStorageSize;
		for (final MultipartFile multipartFile : files) {
			if (multipartFile != null && !multipartFile.isEmpty()) {
				final IFile tmpFile = tmpSubDir.secureExpandPathDown(
						IFile.sanitize(multipartFile.getOriginalFilename()));
				tmpFile.expectFileIsWritable();
				tmpFile.delete();
				multipartFile.transferTo(tmpFile);
				prepare(tmpFile, destinationSubDir, multipartFile.getOriginalFilename(), fileFilter, remainingDownloadSize);
				remainingDownloadSize -= FileUtils.sizeOfDirectory(destinationSubDir);
			}
		}
	}

	private void prepare(final IFile tmpFile, final IFile storageSubDir, final String originalFilename,
			final MultiFileFilter fileFilter, final long maxSize) throws LocalizableApiError {
		final String type;
		try {
			type = MimeTypeUtils.detectMimeType(tmpFile);
		} catch (Exception e) {
			throw new LocalizableApiError("l.upload.invalid", e);
		}

		if (type.equals("application/zip")) {
			// Unzip files to directory
			try {
				tmpFile.unzipTo(storageSubDir, fileFilter, maxSize);
			} catch (IOsizeLimitExceededException e) {
				throw new LocalizableApiError("l.max.extract.size.exceeded", false, 400, e, maxStorageSizeHr);
			} catch (IOException e) {
				throw new LocalizableApiError("l.decompress.failed", e);
			} finally {
				// delete zip file
				tmpFile.delete();
			}
		} else {
			if (!baseFilter.content().accept(type)) {
				tmpFile.delete();
				throw new LocalizableApiError("l.upload.invalid", false, 400, type);
			}
			try {
				tmpFile.moveTo(storageSubDir.getPath() + File.separator + IFile.sanitize(originalFilename));
			} catch (IOException e) {
				throw new LocalizableApiError(e);
			}
		}
	}
}
