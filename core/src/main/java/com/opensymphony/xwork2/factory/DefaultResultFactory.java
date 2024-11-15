/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.opensymphony.xwork2.factory;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.entities.ResultConfig;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.reflection.ReflectionException;
import com.opensymphony.xwork2.util.reflection.ReflectionExceptionHandler;
import com.opensymphony.xwork2.util.reflection.ReflectionProvider;
import org.apache.struts2.factory.StrutsResultFactory;

import java.util.Map;

/**
 * Default implementation
 *
 * @deprecated since 6.7.0, use {@link StrutsResultFactory} instead.
 */
@Deprecated
public class DefaultResultFactory implements ResultFactory {

    private ObjectFactory objectFactory;
    private ReflectionProvider reflectionProvider;

    @Inject
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Inject
    public void setReflectionProvider(ReflectionProvider reflectionProvider) {
        this.reflectionProvider = reflectionProvider;
    }

    public Result buildResult(ResultConfig resultConfig, Map<String, Object> extraContext) throws Exception {
        String resultClassName = resultConfig.getClassName();
        Result result = null;

        if (resultClassName != null) {
            Object o = objectFactory.buildBean(resultClassName, extraContext);

            Map<String, String> params = resultConfig.getParams();
            if (params != null) {
                for (Map.Entry<String, String> paramEntry : params.entrySet()) {
                    try {
                        reflectionProvider.setProperty(paramEntry.getKey(), paramEntry.getValue(), o, extraContext, true);
                    } catch (ReflectionException ex) {
                        if (o instanceof ReflectionExceptionHandler) {
                            ((ReflectionExceptionHandler) o).handle(ex);
                        }
                    }
                }
            }

            if (o instanceof Result) {
                result = (Result) o;
            } else if (o instanceof org.apache.struts2.result.Result) {
                result = Result.adapt((org.apache.struts2.result.Result) o);
            }
            if (result == null) {
                throw new ConfigurationException("Class [" + resultClassName + "] does not implement Result", resultConfig);
            }
        }

        return result;
    }

}
