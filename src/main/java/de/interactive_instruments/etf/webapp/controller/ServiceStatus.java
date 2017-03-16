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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Service status types
 *
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public enum ServiceStatus {
	STARTING, GOOD, MINOR, // increased exception rate, 1 exception found in the last 100 log file lines
	MAJOR, // failed to load component
	MAINTENANCE, SHUTDOWN
}
