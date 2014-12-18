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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-reflection:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-reflection-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect.meta;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;

import org.ldp4j.reflect.model.Parameter;

/**
 * An interface for facilitating the introspection of the parameters of methods
 * or constructors.
 *
 * @author Miguel Esteban Guti&eacute;rrez
 *
 * @see java.lang.reflect.Method
 * @see java.lang.reflect.Constructor
 */
public interface MetaParameter<D extends AccessibleObject & Member & GenericDeclaration> extends MetaAnnotatedElement<Parameter<D>> {

	/**
	 * Get the position of this parameter within the formal parameter list of
	 * the method/constructor where this parameter is defined
	 */
	int getPosition();

	/**
	 * Returns the type of the parameter.
	 */
	MetaClass<?> getType();

	/**
	 * Returns the {@link MetaInvocable} that declares this parameter.
	 */
	MetaInvocable<?,?,?> getInvocable();

}