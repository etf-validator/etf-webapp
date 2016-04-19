/**
 * Copyright 2010-2016 interactive instruments GmbH
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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import com.sun.management.OperatingSystemMXBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.interactive_instruments.IFile;
import de.interactive_instruments.exceptions.config.MissingPropertyException;

/**
 * Controller for reporting the service status
 *
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
@RestController
public class StatusController {

	@Autowired
	private EtfConfigController config;

	private IFile tdDir;

	@PostConstruct
	public void init() throws IOException, JAXBException, MissingPropertyException {
		tdDir = config.getPropertyAsFile(EtfConfigController.ETF_TESTDATA_DIR);
		mbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	}

	private final AtomicReference<HttpHeaders> serviceStatusHeaders = new AtomicReference<>(
			new HttpHeaders() {
				{
					set("status", ServiceStatus.STARTING.toString());
					setLastModified(System.currentTimeMillis());
					setExpires(System.currentTimeMillis() + 2000);
				}
			});

	private final AtomicReference<Map<String, Object>> serviceStatus = new AtomicReference<>(
			new LinkedHashMap<>());

	private OperatingSystemMXBean mbean;

	private final static int updateInterval = 20000;

	@Scheduled(fixedDelay = updateInterval)
	public void watch() {

		final long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		final long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

		final List<String> statusWarningMessages = new ArrayList<>();
		ServiceStatus status = ServiceStatus.GOOD;
		final double usableDiskSpace = ((double) tdDir.getFreeSpace()) / ((double) tdDir.getTotalSpace());
		final double usableMemory = ((double) allocatedMemory) / ((double) Runtime.getRuntime().maxMemory());
		if (tdDir.getFreeSpace() < 5368709120L) {
			statusWarningMessages.add("Less then 5 GB disk space available");
			status = ServiceStatus.MAJOR;
		}
		if (usableDiskSpace < 0.13) {
			statusWarningMessages.add("Less then 13% disk space available");
			if (status != ServiceStatus.MAJOR) {
				status = ServiceStatus.MINOR;
			}
		}
		if (usableMemory > 0.90) {
			statusWarningMessages.add("Less then 10% RAM available");
			status = ServiceStatus.MAJOR;
		}
		if (presumableFreeMemory < 536870912L) {
			statusWarningMessages.add("Less then 512 MB RAM available");
			if (status != ServiceStatus.MAJOR) {
				status = ServiceStatus.MINOR;
			}
		}

		final long modified = System.currentTimeMillis();
		final long expires;
		if (status == ServiceStatus.GOOD) {
			expires = modified + updateInterval * 8;
		} else if (status == ServiceStatus.MINOR) {
			expires = modified + updateInterval * 4;
		} else {
			expires = modified + updateInterval * 2;
		}

		final ServiceStatus finalStatus = status;
		serviceStatusHeaders.set(new HttpHeaders() {
			{
				set("status", finalStatus.toString());
				set("name", config.getProperty(EtfConfigController.ETF_BRANDING_TEXT));
				setLastModified(modified);
				setExpires(expires);
			}
		});

		final Map<String, Object> updatedServiceStatus = new LinkedHashMap<>();
		updatedServiceStatus.put("name", config.getProperty(EtfConfigController.ETF_BRANDING_TEXT));
		updatedServiceStatus.put("status", status);
		updatedServiceStatus.put("heartbeat", modified);
		updatedServiceStatus.put("willExpireAt", expires);
		updatedServiceStatus.put("version", config.getVersion());
		updatedServiceStatus.put("allocatedMemory", allocatedMemory);
		updatedServiceStatus.put("presumableFreeMemory", presumableFreeMemory);
		updatedServiceStatus.put("totalSpace", tdDir.getTotalSpace());
		updatedServiceStatus.put("freeSpace", tdDir.getFreeSpace());
		updatedServiceStatus.put("cpuLoad", String.format("%3f", mbean.getProcessCpuLoad()));
		if (status != ServiceStatus.GOOD) {
			updatedServiceStatus.put("messages", statusWarningMessages);
		}
		serviceStatus.set(updatedServiceStatus);
	}

	@RequestMapping(value = "/v0/heartbeat", method = RequestMethod.HEAD)
	public ResponseEntity<String> simpleHeartbeat() {
		return new ResponseEntity(serviceStatusHeaders.get(), HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("#oauth2.clientHasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/v0/admin/status", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String, Object> getStatus() {
		return serviceStatus.get();
	}

	public void triggerMaintenance() {

	}
}
