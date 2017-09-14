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
package de.interactive_instruments.etf.webapp.conversion;

import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class EtfFormatterRegistrar implements FormatterRegistrar {
	@Override
	public void registerFormatters(final FormatterRegistry registry) {
		addConverter(registry, new EidConverter());
		addConverter(registry, new VersionConverter());
	}

	private static void addConverter(final FormatterRegistry registry, final EtfConverter converter) {
		registry.addConverter(converter.strToTypeConverter());
		registry.addConverter(converter.typeToStrConverter());
		registry.addFormatter(converter);
	}
}
