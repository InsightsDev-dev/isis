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

package org.apache.isis.core.progmodel.facets.collections.disabled.fromimmutable;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * REVIEW: I'm not sure this {@link FacetFactory} actually makes sense. Just
 * because a type is immutable, doesn't imply that the property can't change the
 * instance that it refers to?
 */
public class DisabledFacetForCollectionDerivedFromImmutableTypeFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public DisabledFacetForCollectionDerivedFromImmutableTypeFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final ObjectSpecification spec = getSpecificationLookup().loadSpecification(processMethodContext.getMethod().getDeclaringClass());
        if (spec.containsDoOpFacet(ImmutableFacet.class)) {
            final ImmutableFacet immutableFacet = spec.getFacet(ImmutableFacet.class);
            final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
            FacetUtil.addFacet(new DisabledFacetForCollectionDerivedFromImmutable(immutableFacet, facetHolder));
        }
    }

}
