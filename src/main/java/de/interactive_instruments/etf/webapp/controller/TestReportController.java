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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.concurrent.InvalidStateTransitionException;
import de.interactive_instruments.container.ContainerFactory;
import de.interactive_instruments.container.LazyLoadContainer;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.assembler.AssemblerException;
import de.interactive_instruments.etf.dal.basex.dao.BasexTestReportDao;
import de.interactive_instruments.etf.dal.dao.TestReportDao;
import de.interactive_instruments.etf.dal.dto.plan.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.result.TestReportDto;
import de.interactive_instruments.etf.model.item.EID;
import de.interactive_instruments.etf.model.result.TestReport;
import de.interactive_instruments.etf.model.result.transformation.TransformationException;
import de.interactive_instruments.etf.model.result.transformation.XslReportTransformer;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.etf.webapp.dto.ReportSelections;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StoreException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * Test result controller for viewing and comparing test results
 */
@Controller
public class TestReportController {

	@Autowired
	ServletContext servletContext;

	@Autowired
	EtfConfigController etfConfig;

	private IFile reportDir;
	private IFile stylesheetFile;
	private TestReportDao store;
	private XslReportTransformer comparisonTransformer;
	private final Logger logger = LoggerFactory.getLogger(TestReportController.class);
	private ContainerFactory containerFactory;

	public TestReportController() {

	}

	@PostConstruct
	public void init() throws IOException, TransformerConfigurationException, StoreException,
			ConfigurationException, InvalidStateTransitionException, InitializationException {

		final IFile etfDir = new IFile(servletContext.getRealPath(
				"/WEB-INF/etf"), "ETF");
		etfDir.expectDirIsReadable();
		reportDir = etfConfig.getPropertyAsFile(EtfConstants.ETF_DATASOURCE_DIR).expandPath("obj");

		store = new BasexTestReportDao();
		store.getConfigurationProperties().setPropertiesFrom(etfConfig, true);
		store.init();

		stylesheetFile = etfConfig.getPropertyAsFile(EtfConstants.ETF_REPORTSTYLES_DIR).expandPath("Report.xsl");

		final XslReportTransformer htmlTransformer = new XslReportTransformer("html", stylesheetFile);
		htmlTransformer.init(etfConfig);
		store.registerTransformer(htmlTransformer);

		final XslReportTransformer htmlDownloadTransformer = new XslReportTransformer("html_download", stylesheetFile);
		htmlDownloadTransformer.init(etfConfig);
		store.registerTransformer(htmlDownloadTransformer);

		comparisonTransformer = new XslReportTransformer("html_diff",
				etfConfig.getPropertyAsFile(EtfConstants.ETF_REPORTSTYLES_DIR).expandPath("ReportComparison.xsl"));
		comparisonTransformer.init(etfConfig);

		store.registerTransformer(comparisonTransformer);
	}

	@PreDestroy
	private void shutdown() {
		store.release();
	}

	public synchronized void saveReport(final TestReport report) throws StoreException, AssemblerException, ObjectWithIdNotFoundException {
		this.store.update(this.store.getDtoAssembler().assembleDto(report));
	}

	TestReportDto createReport(String label, TestObjectDto tO) throws StoreException {
		return this.store.create(label, tO);
	}

	void updateReport(TestReport report) throws AssemblerException, StoreException, ObjectWithIdNotFoundException {
		this.store.update(this.store.getDtoAssembler().assembleDto(report));
	}

	@RequestMapping(value = "/reports/{id}", method = RequestMethod.GET)
	public void getById(
			@PathVariable EID id,
			@RequestParam(value = "download", required = false) String download,
			HttpServletResponse response) {

		try {
			final ServletOutputStream out = response.getOutputStream();
			if (Objects.equals(download, "true")) {
				response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
				final TestReportDto dto = store.getDtoById(id);
				final String label = dto.getLabel();
				final String reportFileName = IFile.sanitize(label);
				response.setContentType(MediaType.TEXT_HTML_VALUE);
				response.setHeader("Content-Disposition", "attachment; filename=" + reportFileName + ".html");
				store.transformReportTo(id, "html_download", out);
			} else {
				response.setContentType(MediaType.TEXT_HTML_VALUE);
				store.transformReportTo(id, "html", out);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/reports/{id}/appendices/{appendixId}", method = RequestMethod.GET)
	public synchronized void getAppendixItemById(
			@PathVariable EID id,
			@PathVariable EID appendixId,
			HttpServletResponse response) {
		try {
			final ServletOutputStream out = response.getOutputStream();
			final LazyLoadContainer item = this.store.readAppendixItem(id, appendixId);
			response.setContentType(item.getContentType());
			item.forceLoadAsStream(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/reports", method = RequestMethod.GET)
	public String overview(
			@CookieValue(value = WebAppConstants.TESTDOMAIN_PARAM, defaultValue = "") String testDomain,
			Model model)
					throws ConfigurationException, StoreException {
		if (SUtils.isNullOrEmpty(testDomain)) {
			model.addAttribute("reports", this.store.getAll());
		} else {
			List<TestReportDto> reports = new ArrayList<>();
			this.store.getAll().forEach(p -> {
				if (p != null && p.getTestObject() != null && p.getTestObject().getProperties() != null &&
						Objects.equals(p.getTestObject().getProperties().getProperty(EtfConstants.ETF_TESTDOMAIN_PK), testDomain)) {
					reports.add(p);
				}
			});
			model.addAttribute("reports", reports);
		}

		return "reports/overview";
	}

	@RequestMapping(value = "/reports/{id}/delete", method = RequestMethod.GET)
	public synchronized String delete(@PathVariable EID id) throws StoreException, ObjectWithIdNotFoundException {
		this.store.delete(id);
		return "redirect:/reports";
	}

	@RequestMapping(value = "/reportcomparison", method = RequestMethod.GET)
	public String compare(Model model) throws ConfigurationException, StoreException {
		model.addAttribute(new ReportSelections());
		model.addAttribute("reports", this.store.getAll());
		return "reports/compare";
	}

	@RequestMapping(value = "/reportcomparison/result", method = RequestMethod.POST)
	public void showDiffs(@ModelAttribute("testObject") ReportSelections reportSelections, HttpServletResponse response) throws IOException, TransformationException {
		final ServletOutputStream out = response.getOutputStream();
		response.setContentType(MediaType.TEXT_HTML_VALUE);
		store.diffTo(reportSelections.getReport1(), reportSelections.getReport2(), "html_diff", out);
	}
}
