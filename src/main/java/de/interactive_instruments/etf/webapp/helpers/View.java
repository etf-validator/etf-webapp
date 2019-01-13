/**
 * Copyright 2017-2019 European Union, interactive instruments GmbH
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
package de.interactive_instruments.etf.webapp.helpers;

import java.util.Collection;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.webapp.controller.EtfConfigController;

/**
 * Helper functions for views
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
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
        final String legalNotice = EtfConfigController.getInstance()
                .getProperty(EtfConfigController.ETF_META_PRIVACYSTATEMENT_TEXT);
        return SUtils.isNullOrEmpty(legalNotice) ? null : legalNotice;
    }

    public static String getBaseUrl() {
        return EtfConfigController.getInstance()
                .getProperty(EtfConfigController.ETF_WEBAPP_BASE_URL);
    }

    public static String getCssUrl() {
        return EtfConfigController.getInstance()
                .getProperty(EtfConfigController.ETF_CSS_URL);
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
