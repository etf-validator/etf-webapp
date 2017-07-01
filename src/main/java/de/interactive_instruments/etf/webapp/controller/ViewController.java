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

import static de.interactive_instruments.etf.webapp.controller.EtfConfigController.ETF_MAX_UPLOAD_SIZE;
import static de.interactive_instruments.etf.webapp.controller.EtfConfigController.ETF_TESTREPORTS_LIFETIME_EXPIRATION;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

import java.util.concurrent.TimeUnit;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@Controller
public class ViewController {

	@Autowired
	TestRunController testRunController;

	@Autowired
	EtfConfigController configController;

	@RequestMapping(value = {"/", "/etf", "/index.html"}, method = RequestMethod.GET)
	public String overview(Model model) throws StorageException, ConfigurationException {
		testRunController.addMetaData(model);
		model.addAttribute("maxUploadSizeHr", FileUtils.byteCountToDisplaySize(
				configController.getPropertyAsLong(ETF_MAX_UPLOAD_SIZE)));
		model.addAttribute("maxUploadSize",
				configController.getPropertyAsLong(ETF_MAX_UPLOAD_SIZE));

		final long reportExp = configController.getPropertyAsLong(ETF_TESTREPORTS_LIFETIME_EXPIRATION);
		if(reportExp>0) {
			model.addAttribute("maxTestRunLifetime",
					DurationFormatUtils.formatDurationWords(TimeUnit.MINUTES.toMillis(reportExp),true, true));
		}

		return "etf";
	}

}
