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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.application.engine.context.ApplicationContextOperation;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.CreationPreferences;
import org.ldp4j.application.engine.context.CreationPreferences.InteractionModel;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;
import org.ldp4j.application.engine.context.PublicContainer;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.rdf.Namespaces;
import org.ldp4j.server.data.DataTransformator;
import org.ldp4j.server.data.ResourceResolver;
import org.ldp4j.server.data.UnsupportedMediaTypeException;
import org.ldp4j.server.utils.VariantHelper;
import org.ldp4j.server.utils.VariantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

final class OperationContextImpl implements OperationContext {

	private static final Logger LOGGER=LoggerFactory.getLogger(OperationContextImpl.class);

	private final class OperationContextResourceResolver implements ResourceResolver {

		@Override
		public URI resolveResource(ManagedIndividualId id) {
			URI result=null;
			PublicResource resource=applicationContextOperation.resolveResource(id);
			if(resource!=null) {
				result=base().resolve(resource.path());
				LOGGER.trace("Resolved resource {} URI to '{}'",id,result);
			}
			return result;
		}

		@Override
		public ManagedIndividualId resolveLocation(URI path) {
			PublicResource resource =
				applicationContextOperation.
					resolveResource(base().relativize(path).toString());

			ManagedIndividualId result = null;
			if(resource!=null) {
				result=resource.individualId();
				LOGGER.trace("Resolved location '{}' to resource {}",path,result);
			}
			return result;
		}

	}

	private final ApplicationContext applicationContext;
	private final String             endpointPath;
	private final HttpMethod         method;
	private final UriInfo            uriInfo;
	private final HttpHeaders        headers;
	private final Request            request;
	private final String             entity;

	private ApplicationContextOperation applicationContextOperation;
	private PublicResource              resource;
	private DataSet                     dataSet;

	OperationContextImpl(
		ApplicationContext applicationContext,
		String endpointPath,
		UriInfo uriInfo,
		HttpHeaders headers,
		Request request,
		String entity,
		HttpMethod method) {
		this.applicationContext = applicationContext;
		this.endpointPath = endpointPath;
		this.method = method;
		this.uriInfo=uriInfo;
		this.headers=headers;
		this.request=request;
		this.entity = entity;
	}

	private Variant contentVariant() {
		List<String> requestHeader=
			headers().
				getRequestHeader(HttpHeaders.CONTENT_ENCODING);

		List<Variant> variants=
			Variant.VariantListBuilder.
				newInstance().
				mediaTypes(headers().getMediaType()).
				encodings(requestHeader.toArray(new String[requestHeader.size()])).
				languages(headers().getLanguage()).
				add().
				build();

		return variants.get(0);
	}

	private String slug() {
		List<String> slugs = this.headers.getRequestHeader("Slug");
		String slug=null;
		if(!slugs.isEmpty()) {
			slug=slugs.get(0);
		}
		return slug;
	}

	private InteractionModel interactionModel() {
		InteractionModel result=null;
		for(String linkHeader:this.headers.getRequestHeader(HttpHeaders.LINK)) {
			result= InteractionModelUtils.fromLink(linkHeader);
			if(result!=null) {
				break;
			}
		}
		return result;
	}

	private ResourceResolver resourceResolver() {
		return new OperationContextResourceResolver();
	}

	private URI endpoint() {
		return URI.create(path());
	}

	private String normalizePath(String path) {
		String tPath=path;
		if(tPath==null) {
			tPath="";
		} else {
			tPath = tPath.trim();
		}
		return tPath;
	}

	UriInfo uriInfo() {
		return this.uriInfo;
	}

	HttpHeaders headers() {
		return this.headers;
	}

	Request request() {
		return this.request;
	}

	String entity() {
		return this.entity;
	}

	@Override
	public URI base() {
		String path=this.uriInfo.getPath();
		String prefix="/"+path.substring(0,path.indexOf('/')+1);
		return URI.create(this.uriInfo.getBaseUri().toString().concat(prefix));
	}

	@Override
	public String path() {
		String path=this.uriInfo.getPath();
		return path.substring(path.indexOf('/')+1);
	}

	@Override
	public boolean isQuery() {
		return !this.uriInfo.getQueryParameters().isEmpty();
	}

	@Override
	public boolean hasQueryParameter(String parameter) {
		return this.uriInfo.getQueryParameters().get(parameter)!=null;
	}

	@Override
	public List<String> getQueryParameters() {
		return ImmutableList.copyOf(this.uriInfo.getQueryParameters().keySet());
	}

	@Override
	public List<String> getQueryParameterValues(String parameter) {
		List<String> result = this.uriInfo.getQueryParameters().get(parameter);
		if(result==null) {
			result=Lists.newArrayList();
		}
		return ImmutableList.copyOf(result);
	}

	@Override
	public OperationContext checkContents() {
		List<Variant> supportedVariants=VariantUtils.defaultVariants();
		if(entity()==null || entity().isEmpty()) {
			throw new MissingContentException(this.resource,this);
		}
		if(headers().getMediaType()==null) {
			throw new MissingContentTypeException(this.resource,this);
		}
		if(!VariantHelper.
				forVariants(supportedVariants).
					isSupported(contentVariant())) {
			throw new UnsupportedContentException(this.resource,this,contentVariant());
		}
		return this;
	}

	@Override
	public OperationContext checkPreconditions() {
		EntityTag entityTag=this.resource.entityTag();
		Date lastModified=this.resource.lastModified();
		if(HttpMethod.PUT.equals(this.method)) {
			List<String> requestHeader = this.headers.getRequestHeader(HttpHeaders.IF_MATCH);
			if((requestHeader==null || requestHeader.isEmpty())) {
				throw new PreconditionRequiredException(this.resource);
			}
		}
		ResponseBuilder builder =
			request().
				evaluatePreconditions(
					lastModified,
					new javax.ws.rs.core.EntityTag(entityTag.getValue()));
		if(builder!=null) {
			Response response = builder.build();
			throw new PreconditionFailedException(this.resource,this,response.getStatus());
		}
		return this;
	}

	@Override
	public OperationContext checkOperationSupport() {
		boolean allowed=false;
		switch(method) {
		case GET:
			allowed=true;
			break;
		case HEAD:
			allowed=true;
			break;
		case OPTIONS:
			allowed=true;
			break;
		case DELETE:
			allowed=resource().capabilities().isDeletable();
			break;
		case PATCH:
			allowed=resource().capabilities().isPatchable();
			break;
		case POST:
			allowed=resource().capabilities().isFactory();
			break;
		case PUT:
			allowed=resource().capabilities().isModifiable();
			break;
		}
		if(!allowed) {
			throw new MethodNotAllowedException(this,this.resource,this.method);
		}
		return this;
	}

	@Override
	public DataSet dataSet() {
		if(this.dataSet==null) {
			MediaType mediaType=contentVariant().getMediaType();
			try {
				DataTransformator transformator =
					DataTransformator.
						create(base()).
						enableResolution(resourceResolver()).
						mediaType(mediaType);
				if(this.method.equals(HttpMethod.POST)) {
					transformator=transformator.surrogateEndpoint(endpoint());
				} else {
					transformator=transformator.permanentEndpoint(endpoint());
				}
				this.dataSet=transformator.unmarshall(this.entity);
			} catch(UnsupportedMediaTypeException e) {
				throw new UnsupportedContentException(this.resource,this,contentVariant());
			} catch(IOException e) {
				throw new InvalidRequestContentException("Entity cannot be parsed as '"+mediaType+"' ("+Throwables.getRootCause(e).getMessage()+")",this.resource,this);
			}
		}
		return this.dataSet;
	}

	@Override
	public Variant expectedVariant() {
		List<Variant> variants=VariantUtils.defaultVariants();
		Variant variant = request.selectVariant(variants);
		if(variant==null) {
			throw new NotAcceptableException(this.resource,this);
		}
		return variant;
	}

	@Override
	public CreationPreferences creationPreferences() {
		return
			CreationPreferences.
				builder().
					withInteractionModel(interactionModel()).
					withPath(slug()).
					build();
	}

	@Override
	public URI resolve(PublicResource resource) {
		return base().resolve(resource.path());
	}

	@Override
	public String serialize(DataSet representation, Namespaces namespaces, MediaType mediaType) {
		try {
			DataTransformator transformator =
				DataTransformator.
					create(base()).
					enableResolution(resourceResolver()).
					mediaType(mediaType).
					namespaces(namespaces).
					permanentEndpoint(endpoint());
			return transformator.marshall(representation);
		} catch(UnsupportedMediaTypeException e) {
			throw new UnsupportedContentException(this.resource,this,contentVariant());
		} catch(IOException e) {
			throw new ContentProcessingException("Resource representation cannot be parsed as '"+mediaType+"' ",this.resource,this);
		}
	}

	@Override
	public PublicResource resource() {
		if(this.resource==null) {
			this.resource=
				this.applicationContextOperation.
					findResource(
						normalizePath(this.endpointPath));
		}
		return this.resource;
	}

	@Override
	public PublicContainer container() {
		PublicResource tmp = resource();
		checkState(tmp instanceof PublicContainer,"Expected an instance of class %s but got an instance of class %s",PublicContainer.class.getCanonicalName(),tmp.getClass().getCanonicalName());
		return (PublicContainer)tmp;
	}

	@Override
	public ContentPreferences contentPreferences() {
		ContentPreferences result = null;
		List<String> requestHeader = this.headers.getRequestHeader(ContentPreferencesUtils.PREFER_HEADER);
		for(Iterator<String> it=requestHeader.iterator();it.hasNext() && result==null;) {
			try {
				String header = it.next();
				result=ContentPreferencesUtils.fromPreferenceHeader(header);
			} catch (InvalidPreferenceHeaderException e) {
				// Ignore
			}
		}
		return result;
	}

	@Override
	public void startOperation() {
		this.applicationContextOperation=
			this.applicationContext.
					createOperation(
						DefaultHttpRequest.
							create(
								this.method,
								this.uriInfo,
								this.headers,
								this.entity,
								new Date(),
								this.headers.getDate()));
	}

	@Override
	public void completeOperation() {
		checkState(this.applicationContextOperation!=null,"Operation not started");
		this.applicationContextOperation.dispose();
	}

}