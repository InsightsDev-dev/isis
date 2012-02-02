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

package org.apache.isis.runtimes.dflt.objectstores.sql.auto;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.AbstractMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.CollectionMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.DatabaseConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.FieldMappingLookup;
import org.apache.isis.runtimes.dflt.objectstores.sql.IdMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.Sql;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcObjectReferenceMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.ObjectReferenceMapping;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistorUtil;

public class AutoCollectionMapper extends AbstractMapper implements CollectionMapper {
    private static final Logger LOG = Logger.getLogger(AutoCollectionMapper.class);
    private String tableName;
    private final ObjectAssociation field;
    private final ObjectReferenceMapping elementMapping;
    private final IdMapping idMapping;

    public AutoCollectionMapper(final ObjectSpecification specification, final ObjectAssociation field, final FieldMappingLookup lookup) {
        this.field = field;

        final ObjectSpecification spec = field.getFacet(TypeOfFacet.class).valueSpec();
        idMapping = lookup.createIdMapping();
        elementMapping = lookup.createMapping(spec);

        final String className = specification.getShortIdentifier();
        final String columnName = field.getId();
        tableName = Sql.sqlName(className) + "_" + asSqlName(columnName);
        tableName = Sql.identifier(tableName);
    }

    @Override
    public void createTables(final DatabaseConnector connector) {
        if (!connector.hasTable(tableName)) {
            final StringBuffer sql = new StringBuffer();
            sql.append("create table ");
            sql.append(tableName);
            sql.append(" (");

            idMapping.appendColumnDefinitions(sql);
            sql.append(", ");
            elementMapping.appendColumnDefinitions(sql);

            sql.append(")");

            connector.update(sql.toString());
        }
    }

    @Override
    public void loadInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent, final boolean makeResolved) {
        final ObjectAdapter collection = field.get(parent);
        if (collection.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            LOG.debug("loading internal collection " + field);
            collection.changeState(ResolveState.RESOLVING);

            final StringBuffer sql = new StringBuffer();
            sql.append("select ");
            idMapping.appendColumnNames(sql);
            sql.append(", ");
            elementMapping.appendColumnNames(sql);
            sql.append(" from ");
            sql.append(tableName);

            final Results rs = connector.select(sql.toString());
            final List<ObjectAdapter> list = new ArrayList<ObjectAdapter>();
            while (rs.next()) {
                final ObjectAdapter element = ((JdbcObjectReferenceMapping) elementMapping).initializeField(rs);
                LOG.debug("  element  " + element.getOid());
                list.add(element);
            }
            final CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
            collectionFacet.init(collection, list.toArray(new ObjectAdapter[list.size()]));
            rs.close();
            if (makeResolved) {
                PersistorUtil.end(collection);
            }
        }
    }

    @Override
    public boolean needsTables(final DatabaseConnector connector) {
        return !connector.hasTable(tableName);
    }

    @Override
    public void saveInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        final ObjectAdapter collection = field.get(parent);
        LOG.debug("saving internal collection " + collection);

        StringBuffer sql = new StringBuffer();
        sql.append("delete from ");
        sql.append(tableName);
        sql.append(" where ");
        idMapping.appendWhereClause(connector, sql, parent.getOid());
        connector.update(sql.toString());

        sql = new StringBuffer();
        sql.append("insert into ");
        sql.append(tableName);
        sql.append(" (");
        idMapping.appendColumnNames(sql);
        sql.append(", ");
        elementMapping.appendColumnNames(sql);
        sql.append(" ) values (");
        idMapping.appendInsertValues(connector, sql, parent);
        sql.append(", ");

        final CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
        for (final ObjectAdapter element : collectionFacet.iterable(collection)) {
            final StringBuffer values = new StringBuffer();
            elementMapping.appendInsertValues(connector, values, element);
            connector.update(sql.toString() + values + ")");
        }
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln(field.getName(), "collection");
        debug.indent();
        debug.appendln("Table", tableName);
        debug.appendln("ID mapping", idMapping);
        debug.appendln("Element mapping", elementMapping);
        debug.unindent();
    }

}
