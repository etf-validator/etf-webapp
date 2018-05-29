/**
 * Copyright 2017-2018 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
 */
package de.interactive_instruments.etf.webapp.controller;

import static de.interactive_instruments.etf.webapp.controller.EtfConfigController.ETF_MAX_UPLOAD_SIZE;
import static de.interactive_instruments.etf.webapp.controller.EtfConfigController.ETF_TESTREPORTS_LIFETIME_EXPIRATION;

import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

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
		if (reportExp > 0) {
			model.addAttribute("maxTestRunLifetime",
					DurationFormatUtils.formatDurationWords(TimeUnit.MINUTES.toMillis(reportExp), true, true));
		}

		return "etf";
	}

}
