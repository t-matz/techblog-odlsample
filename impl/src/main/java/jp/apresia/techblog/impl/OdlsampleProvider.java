/*
 * Copyright © 2024 APRESIA Systems, Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package jp.apresia.techblog.impl;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlsample.rev240304.Sample;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlsample.rev240304.SampleBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlsample.rev240304.sample.grp.DataList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlsample.rev240304.sample.grp.DataListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlsample.rev240304.sample.grp.DataListKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OdlsampleProvider.
 */
public final class OdlsampleProvider implements DataTreeChangeListener<Sample>, AutoCloseable {

    private final DataBroker dataBroker;
    private final ListenerRegistration<OdlsampleProvider> registration;
    private static final Logger LOG = LoggerFactory.getLogger(OdlsampleProvider.class);
    private static final InstanceIdentifier<Sample> SAMPLE_PATH = InstanceIdentifier.create(Sample.class);

    public OdlsampleProvider(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.registration = this.dataBroker
                .registerDataTreeChangeListener(
                        DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, SAMPLE_PATH), this);
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
        if (this.registration != null) {
            this.registration.close();
        }
    }

    @Override
    public void onDataTreeChanged(@NonNull Collection<DataTreeModification<Sample>> changes) {
        WriteTransaction wtx = dataBroker.newWriteOnlyTransaction();
        for (DataTreeModification<Sample> change : changes) {
            LOG.trace("change, {}", change);
            final DataObjectModification<Sample> root = change.getRootNode();
            updateData(wtx, root.getModifiedChildren());
        }
    }

    private void updateData(
            WriteTransaction wtx,
            @NonNull Collection<? extends DataObjectModification<? extends DataObject>> changes) {
        var modifiedDataList = new HashMap<DataListKey, DataList>();
        for (DataObjectModification<? extends DataObject> change : changes) {
            DataObject after = change.getDataAfter();
            if (after == null) {
                DataObject before = change.getDataBefore();
                if (!(before instanceof DataList)) {
                    continue;
                }

                DataList beforeDataList = (DataList) before;
                LOG.trace("Deleted ID, {}", beforeDataList.getId());
                wtx.delete(LogicalDatastoreType.OPERATIONAL,
                        SAMPLE_PATH.child(DataList.class, new DataListKey(beforeDataList.getId())));
            } else {
                if (!(after instanceof DataList)) {
                    continue;
                }
                DataList afterDataList = (DataList) after;
                var key = new DataListKey(afterDataList.getId());
                String timeStamp = new SimpleDateFormat("yyyy/MM/dd.HH:mm:ss").format(new Date());
                DataListBuilder builder = new DataListBuilder(afterDataList);
                builder.setLastModified(timeStamp);
                LOG.trace("Modified ID, {}", afterDataList.getId());
                modifiedDataList.put(key, builder.build());
            }
        }

        Sample modifiedSample = new SampleBuilder().setDataList(modifiedDataList).build();
        wtx.merge(LogicalDatastoreType.OPERATIONAL, SAMPLE_PATH, modifiedSample);
        commitWrapper(wtx.commit());
    }

    private void commitWrapper(FluentFuture<? extends Object> writeResult) {
        writeResult.addCallback(new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object noarg) {
                LOG.debug("Successfully committed.");
            }

            @Override
            public void onFailure(Throwable error) {
                LOG.debug("Error while committing.", error);
            }
        }, MoreExecutors.directExecutor());

    }
}
