/**
 * Copyright 2010-2022 interactive instruments GmbH
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

import static de.interactive_instruments.etf.webapp.controller.EtfConfig.ETF_MAX_UPLOAD_SIZE;
import static de.interactive_instruments.etf.webapp.controller.EtfConfig.ETF_TESTREPORTS_LIFETIME_EXPIRATION;

import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@Controller
public class ViewController {

    @Autowired
    TestRunController testRunController;

    @Autowired
    EtfConfig etfConfig;

    @RequestMapping(value = {"/", "/etf", "/index.html"}, method = RequestMethod.GET)
    public String overview(Model model) throws ConfigurationException {
        testRunController.addMetaData(model);
        model.addAttribute("maxUploadSizeHr", FileUtils.byteCountToDisplaySize(
                etfConfig.getPropertyAsLong(ETF_MAX_UPLOAD_SIZE)));
        model.addAttribute("maxUploadSize",
                etfConfig.getPropertyAsLong(ETF_MAX_UPLOAD_SIZE));
        final long reportExp = etfConfig.getPropertyAsLong(ETF_TESTREPORTS_LIFETIME_EXPIRATION);
        if (reportExp > 0) {
            final Period period = new Period(TimeUnit.MINUTES.toMillis(reportExp));
            model.addAttribute("maxTestRunLifetime",
                    PeriodFormat.wordBased(LocaleContextHolder.getLocale()).print(period.normalizedStandard()));
        }
        return "etf";
    }

}
