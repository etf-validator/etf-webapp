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
package de.interactive_instruments.etf.webapp.helpers;

import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.webapp.controller.EtfConfigController;
import de.interactive_instruments.exceptions.ExcUtils;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
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
		final String userNamePrefix;
		if(!SUtils.isNullOrEmpty(request.getRemoteUser())) {
			userNamePrefix=request.getRemoteUser()+"@";
		}else{
			userNamePrefix="";
		}
		return userNamePrefix+getRemoteAddr(request);
	}

	private static String getRemoteAddr(final HttpServletRequest request) {
		if ("false".equals(EtfConfigController.getInstance().getPropertyOrDefault("etf.users.log", "false"))) {
			return "unknown";
		}
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
