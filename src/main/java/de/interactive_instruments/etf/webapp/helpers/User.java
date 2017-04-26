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
package de.interactive_instruments.etf.webapp.helpers;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.exceptions.ExcUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.UnknownHostException;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public class User {

	private static String[] evalProxyHeaders = new String[]{
			"X-Forwarded-For",
			"HTTP_X_FORWARDED_FOR",
			"X-Real-IP",
			"Proxy-Client-IP",
			"WL-Proxy-Client-IP",
			"HTTP_CLIENT_IP"
	};

	public static String getUser(final HttpServletRequest request) {
		final String remoteAddr = request.getRemoteAddr();
		try {
			if (UriUtils.isPrivateNet(remoteAddr)) {
				for (final String evalProxyHeader : evalProxyHeaders) {
					final String h = request.getHeader(evalProxyHeader);
					if (!SUtils.isNullOrEmpty(h)) {
						final String firstIp = SUtils.leftOfSubStrOrNull(h, ",");
						if (firstIp != null) {
							return firstIp;
						}
						return h;
					}
				}
			}
		} catch (final UnknownHostException e) {
			ExcUtils.suppress(e);
		}
		return remoteAddr;
	}
}
