/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.validation.ValidationConstraintFactory;
import org.ldp4j.application.data.validation.Validator.ValidatorBuilder;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.template.DirectContainerTemplate;
import org.ldp4j.application.vocabulary.Term;

public final class PublicDirectContainer extends PublicMembershipAwareContainer<DirectContainerTemplate> {

	protected PublicDirectContainer(ApplicationContext applicationContext, Endpoint endpoint) {
		super(applicationContext, endpoint, DirectContainerTemplate.class);
	}
	
	@Override
	public <T> T accept(PublicVisitor<T> visitor) {
		return visitor.visitDirectContainer(this);
	}

	@Override
	protected Term containerType() {
		return LDP.DIRECT_CONTAINER;
	}

	@Override
	protected void fillInMetadata(ContentPreferences contentPreferences, Individual<?, ?> individual, Context ctx) {
		super.fillInMetadata(contentPreferences, individual, ctx);
		individual.addValue(
			ctx.property(LDP.INSERTED_CONTENT_RELATION), 
			ctx.reference(LDP.MEMBER_SUBJECT));
	}

	@Override
	protected void configureValidationConstraints(ValidatorBuilder builder, Individual<?, ?> individual, DataSet metadata) {
		super.configureValidationConstraints(builder, individual, metadata);
		builder.withPropertyConstraint(ValidationConstraintFactory.readOnlyProperty(individual.property(LDP.INSERTED_CONTENT_RELATION.as(URI.class))));
	}

}