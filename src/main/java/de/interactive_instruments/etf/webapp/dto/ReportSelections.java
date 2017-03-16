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
package de.interactive_instruments.etf.webapp.dto;

import java.util.UUID;

import de.interactive_instruments.etf.model.EID;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
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
