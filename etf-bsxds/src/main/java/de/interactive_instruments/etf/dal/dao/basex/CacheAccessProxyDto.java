/**
 * Copyright 2010-2020 interactive instruments GmbH
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
package de.interactive_instruments.etf.dal.dao.basex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import org.slf4j.Logger;

import de.interactive_instruments.etf.model.EID;

/**
 * Cache access proxy for Dtos
 *
 * Must be public and non-final!
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class CacheAccessProxyDto {

    private final BsxDsCtx ctx;
    private final Logger logger;

    CacheAccessProxyDto(final BsxDsCtx ctx, final Logger logger) {
        this.ctx = ctx;
        this.logger = logger;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @This ProxyAccessor proxy, @AllArguments Object[] args) {
        logger.trace("Intercepted {} method call", method.getName());
        if (proxy.getCached() == null) {
            if (proxy.getProxiedId() == null) {
                throw new IllegalStateException("Eid not set");
            }
            proxy.setCached(ctx.getFromCache(proxy.getProxiedId()));
            if (proxy.getCached() == null) {
                throw new IllegalStateException("Unable to load cached Dto " + proxy.getProxiedId() + " ! "
                        + "This may happen if the root object of this reference is not loaded!");
            }
        }
        try {
            return method.invoke(proxy.getCached(), args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to proxy Dto " + proxy.getProxiedId() + " method call", e);
        }
    }

    @RuntimeType
    public EID getId(@This ProxyAccessor proxy) {
        if (proxy.getCached() != null) {
            return proxy.getCached().getId();
        }
        return proxy.getProxiedId();
    }

    @RuntimeType
    public String getDescriptiveLabel(@This ProxyAccessor proxy) {
        return "\'CACHEACCESS." + proxy.getProxiedId() + "\'";
    }

    @RuntimeType
    public String toString(@This ProxyAccessor proxy) {
        final StringBuilder sb = new StringBuilder("CacheAccessProxy{");
        sb.append("id=").append(proxy.getProxiedId()).append(", proxies=");
        if (proxy.getCached() != null) {
            sb.append(proxy.getCached().toString());
        } else {
            sb.append("UNRESOLVED");
        }
        sb.append('}');
        return sb.toString();
    }
}
