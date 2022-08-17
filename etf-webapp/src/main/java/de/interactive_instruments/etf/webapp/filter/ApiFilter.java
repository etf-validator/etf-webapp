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
package de.interactive_instruments.etf.webapp.filter;

import static de.interactive_instruments.etf.webapp.controller.EtfConfig.ETF_API_ALLOW_ORIGIN;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import de.interactive_instruments.etf.webapp.controller.EtfConfig;
import de.interactive_instruments.etf.webapp.controller.LocalizableApiError;
import de.interactive_instruments.etf.webapp.controller.StatusController;
import de.interactive_instruments.etf.webapp.dto.ApiError;
import de.interactive_instruments.exceptions.ExcUtils;

@Component("ApiFilter")
public class ApiFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiFilter.class);

    private final EtfConfig etfConfig;
    private final StatusController status;
    private final ApplicationContext applicationContext;

    private String allowOrigin;

    public ApiFilter(final EtfConfig etfConfig, final StatusController status, final ApplicationContext applicationContext) {
        this.etfConfig = etfConfig;
        this.status = status;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() {
        allowOrigin = this.etfConfig.getPropertyOrDefault(ETF_API_ALLOW_ORIGIN, "localhost");
        logger.info("API Access-Control-Allow-Origin is set to: {}", allowOrigin);
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain)
            throws IOException {

        if (status.inShutdownMode() && "POST".equals(request.getMethod())) {
            response.setStatus(423);
            response.setHeader("Content-Type", "application/json");
            final ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), new ApiError(new LocalizableApiError(
                    "l.shutdown.prepared", false, 423), request.getRequestURL().toString(), applicationContext));
            response.getWriter().flush();
            return;
        }

        if (!"localhost".equals(this.allowOrigin)) {
            response.addHeader("Access-Control-Allow-Origin", this.allowOrigin);
            if (!"*".equals(this.allowOrigin)) {
                // If the server specifies an origin host rather than "*", then it must also include Origin in the Vary
                // response header to indicate to clients that server responses will differ based on the value
                // of the Origin request header.
                response.addHeader("Vary", "Origin");
            }
            if (request.getHeader("Access-Control-Request-Method") != null && "OPTIONS".equals(request.getMethod())) {
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                response.addHeader("Access-Control-Allow-Credentials", "true");
                response.addHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Authorization");
                response.addHeader("Access-Control-Max-Age", "60");
                response.getWriter().print("OK");
                response.getWriter().flush();
                return;
            }
        }
        try {
            filterChain.doFilter(request, response);
        } catch (final ServletException e) {
            if (e.getRootCause() instanceof MaxUploadSizeExceededException) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e1) {
                    ExcUtils.suppress(e1);
                }
                final ObjectMapper mapper = new ObjectMapper();
                response.setStatus(413);
                response.setHeader("Content-Type", "application/json");
                mapper.writeValue(response.getWriter(), new ApiError(new LocalizableApiError(
                        "l.max.upload.size.exceeded", false, 413), request.getRequestURL().toString(), applicationContext));
                response.getWriter().flush();
            }
        }
    }

}
