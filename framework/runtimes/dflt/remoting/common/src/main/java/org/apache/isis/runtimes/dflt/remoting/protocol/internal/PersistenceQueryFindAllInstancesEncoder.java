/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.remoting.protocol.internal;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.remoting.common.data.query.PersistenceQueryData;
import org.apache.isis.runtimes.dflt.remoting.common.data.query.PersistenceQueryFindAllInstancesData;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;

public class PersistenceQueryFindAllInstancesEncoder extends PersistenceQueryEncoderAbstract {

    @Override
    public Class<?> getPersistenceQueryClass() {
        return PersistenceQueryFindAllInstances.class;
    }

    @Override
    public PersistenceQueryData encode(final PersistenceQuery persistenceQuery) {
        return new PersistenceQueryFindAllInstancesData(persistenceQuery.getSpecification());
    }

    @Override
    protected PersistenceQuery doDecode(final ObjectSpecification specification, final PersistenceQueryData persistenceQueryData) {
        return new PersistenceQueryFindAllInstances(specification);
    }

    private PersistenceQueryFindAllInstances downcast(final PersistenceQuery persistenceQuery) {
        return (PersistenceQueryFindAllInstances) persistenceQuery;
    }

}
