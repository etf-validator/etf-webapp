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
package de.interactive_instruments.etf.webapp.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.webapp.controller.EtfConfigController;

/**
 * Helper functions for views
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public class View {

	private View() {

	}

	public static Collection<String[]> getTestRunParams(final ExecutableTestSuiteDto ets) {
		return ets.getParameters().asNameDefaultValuePairs();
	}

	public static boolean hasTestRunParams(final ExecutableTestSuiteDto ets) {
		return !ets.getParameters().isEmpty();
	}

	public static String getWorkflowType() {
		return EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_WORKFLOWS);
	}

	public static String getBrandingText() {
		return EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_BRANDING_TEXT);
	}

	public static String getContactText() {
		final String disclaimer = EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_META_CONTACT_TEXT);
		return SUtils.isNullOrEmpty(disclaimer) ? null : disclaimer;
	}

	public static String getDisclaimerText() {
		final String legalNotice = EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_META_DISCLAIMER_TEXT);
		return SUtils.isNullOrEmpty(legalNotice) ? null : legalNotice;
	}

	public static String getCopyrightText() {
		final String disclaimer = EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_META_COPYRIGHT_TEXT);
		return SUtils.isNullOrEmpty(disclaimer) ? null : disclaimer;
	}

	public static String getPrivacyStatementText() {
		final String legalNotice = EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_META_PRIVACYSTATEMENT_TEXT);
		return SUtils.isNullOrEmpty(legalNotice) ? null : legalNotice;
	}

	public static String getHelpPageURL() {
		return EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_HELP_PAGE_URL);
	}

	public static String getVersion() {
		return EtfConfigController.getInstance().getVersion();
	}

	public static String getReportComparison() {
		return EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_REPORT_COMPARISON);
	}

	public static String getSubmitAnalysisData() {
		return EtfConfigController.getInstance().getProperty(EtfConfigController.ETF_SUBMIT_ERRORS);
	}

}
