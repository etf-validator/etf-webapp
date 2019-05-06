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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpHeaders;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dto.Dto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class CacheControl {
    private CacheControl() {}

    private static TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String[] DATE_PATTERNS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };

    /**
     * Checks if the client send a request with the "If-Modified-Since" header and compares it with the last modification date of the corresponding dao.
     *
     * Will set the last-modified and cache-control header.
     *
     * If the Client sets the Cache-Control header to no-cache or max-age 0, true is returned.
     *
     * @return true if client needs an update, false otherwise
     */
    public static boolean clientNeedsUpdate(final Dao<? extends Dto> dao, final HttpServletRequest request,
            final HttpServletResponse response, final long maxAge) {

        if (SUtils.compareNullSafeIgnoreCase(
                request.getParameter("nocache"), "true") == 0 ||
                SUtils.compareNullSafeIgnoreCase(
                        request.getHeader("Cache-Control"), "no-cache") == 0
                ||
                SUtils.compareNullSafeIgnoreCase(
                        request.getHeader("Cache-Control"), "max-age=0") == 0) {
            return true;
        }
        final Calendar lastModified = Calendar.getInstance(GMT);
        lastModified.setTimeInMillis(dao.getLastModificationDate());
        lastModified.set(Calendar.MILLISECOND, 0);
        final Date lastModifiedDate = lastModified.getTime();

        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(GMT);
        response.setHeader(HttpHeaders.LAST_MODIFIED, dateFormat.format(lastModifiedDate));
        setCache(maxAge, response);

        final String ifModifiedSince = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
        if (SUtils.isNullOrEmpty(ifModifiedSince)) {
            return true;
        }
        final Date ifModifiedSinceDate;
        try {
            ifModifiedSinceDate = DateUtils.parseDate(ifModifiedSince, DATE_PATTERNS);
        } catch (ParseException e) {
            return true;
        }
        if (lastModifiedDate.equals(ifModifiedSinceDate)) {
            // Not modified
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return false;
        } else {
            // Client gets updated content
            return true;
        }
    }

    public static boolean clientNeedsUpdate(final Dao<? extends Dto> dao, final HttpServletRequest request,
            final HttpServletResponse response) {
        return clientNeedsUpdate(dao, request, response, 120);
    }

    public static void setCache(final long maxAge, final HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + String.valueOf(maxAge));
    }

}
