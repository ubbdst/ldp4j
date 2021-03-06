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
package org.ldp4j.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Member;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.resource.ResourceVisitor;
import org.ldp4j.application.template.ContainerTemplate;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

final class InMemoryContainer extends InMemoryResource implements Container {

	private static final class InMemoryMember implements Member {

		private final ResourceId memberId;
		private final ResourceId containerId;
		private final long number;

		private InMemoryMember(ResourceId containerId, ResourceId memberId, long number) {
			this.memberId = memberId;
			this.containerId = containerId;
			this.number = number;
		}

		@Override
		public long number() {
			return number;
		}

		@Override
		public ResourceId containerId() {
			return containerId;
		}

		@Override
		public ResourceId memberId() {
			return memberId;
		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						add("number",this.number).
						add("containerId",this.containerId).
						add("memberId", this.memberId).
						toString();
		}

	}

	private final ConcurrentMap<ResourceId,Member> members;
	private final AtomicLong memberCounter;

	protected InMemoryContainer(ResourceId id, ResourceId parentId) {
		super(id,parentId);
		this.members=Maps.newConcurrentMap();
		this.memberCounter=new AtomicLong();
	}

	protected InMemoryContainer(ResourceId id) {
		this(id,null);
	}

	private Member createMember(InMemoryResource newResource) {
		InMemoryMember member = new InMemoryMember(id(), newResource.id(), this.memberCounter.incrementAndGet());
		Member result = this.members.putIfAbsent(member.memberId(), member);
		if(result==null) {
			result=member;
		}
		return result;
	}

	private InMemoryResource createMemberResource(ResourceId resourceId) {
		checkNotNull(resourceId,"Member resource identifier cannot be null");
		checkState(!this.members.containsKey(resourceId),"A resource with id '%s' is already a member of the container",resourceId);
		InMemoryResource newResource=createChild(resourceId,template().memberTemplate());
		return newResource;
	}

	private ContainerTemplate template() {
		return (ContainerTemplate)super.getTemplate(id());
	}

	@Override
	public void accept(ResourceVisitor visitor) {
		visitor.visitContainer(this);
	}

	@Override
	public Resource addMember(ResourceId resourceId) {
		InMemoryResource newResource = createMemberResource(resourceId);
		createMember(newResource);
		return newResource;
	}

	@Override
	public boolean hasMember(ResourceId resource) {
		return this.members.containsKey(resource);
	}

	@Override
	public Set<Member> members() {
		return ImmutableSet.copyOf(members.values());
	}

	@Override
	public Member findMember(ResourceId resourceId) {
		return this.members.get(resourceId);
	}

	@Override
	public boolean removeMember(Member member) {
		checkNotNull(member,"Member cannot be null");
		return members.remove(member.memberId(), member);
	}

	@Override
	public String toString() {
		return
			stringHelper().
				add("memberCounter",this.memberCounter).
				add("members",this.members).
				toString();
	}

}