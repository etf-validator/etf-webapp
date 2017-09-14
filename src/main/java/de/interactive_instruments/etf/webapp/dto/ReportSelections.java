/**
 * Copyright 2017 European Union, interactive instruments GmbH
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
package de.interactive_instruments.etf.webapp.dto;

import de.interactive_instruments.etf.model.EID;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class ReportSelections {
	private EID report1;
	private EID report2;

	public EID getReport1() {
		return report1;
	}

	public void setReport1(EID report1) {
		this.report1 = report1;
	}

	public EID getReport2() {
		return report2;
	}

	public void setReport2(EID report2) {
		this.report2 = report2;
	}
}
