/*
 * Copyright © 2024 APRESIA Systems, Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package jp.apresia.techblog.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlsample.rev240304.SampleNotify;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SampleNotificationListener implements Listener<SampleNotify>, AutoCloseable {
    private final NotificationService notifyService;
    private final Registration registration;
    private static final Logger LOG = LoggerFactory.getLogger(SampleNotificationListener.class);

    public SampleNotificationListener(final NotificationService notifyService) {
        this.notifyService = notifyService;
        this.registration = this.notifyService.registerListener(SampleNotify.class, this);
    }

    @Override
    public void onNotification(@NonNull SampleNotify notification) {
        LOG.info("MESSAGE RECIEVED: {}.", notification.getMessage());
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("SampleNotificationListener Initiated");
    }

    @Override
    public void close() throws Exception {
        LOG.info("SampleNotificationListener Closed");
        if (this.registration != null) {
            this.registration.close();
        }
    }

}
