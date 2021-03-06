/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.apiml;

import com.ca.mfaas.eurekaservice.client.config.Ssl;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("server.ssl")
public class SslConfigBean extends Ssl {
}
