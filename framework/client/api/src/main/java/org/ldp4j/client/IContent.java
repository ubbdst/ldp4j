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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client;

import java.io.IOException;

/**
 * A {@code IContent} represents formatted contents of a {@code Representation}
 * of a <b>Linked Data Platform Resource</b> that allows its
 * {@link #serialize(Class) serialization} in different ways.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.client.IRepresentation#getContent()
 */
public interface IContent {

	/**
	 * Retrieve the raw contents.
	 * @return The raw contents.
	 */
	Object getRawSource();
	
	/**
	 * Serialize the contents.
	 * 
	 * @param clazz
	 *            The type to which the content has to be serialized.
	 * @return An instance of the specified class that allows accessing to the
	 *         contents.
	 * @throws IOException
	 *             If the content cannot be serialized to an instance of the
	 *             specified class.
	 */
	<S> S serialize(Class<S> clazz) throws IOException;
	
}