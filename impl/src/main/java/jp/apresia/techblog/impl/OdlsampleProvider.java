/*
 * Copyright © 2024 APRESIA Systems, Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package jp.apresia.techblog.impl;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OdlsampleProvider.
 */
public final class OdlsampleProvider implements AutoCloseable {

    private final DataBroker dataBroker;
    private static final Logger LOG = LoggerFactory.getLogger(OdlsampleProvider.class);

    public OdlsampleProvider(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("OdlsampleProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    @Override
    public void close() throws Exception {
        LOG.info("OdlsampleProvider Closed");
    }
}
