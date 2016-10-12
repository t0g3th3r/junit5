/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Method} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on Java methods.
 *
 *  If a java {@link Method} is provided, the selector will return this
 * {@link Method} and its method name accordingly. If the selector was
 * created with a {@link Class} and {@link Method} name, a {@link Class}
 * name and {@link Method} name, or only a full qualified {@link Method}
 * name, it will tries to lazy load the {@link Class} and {@link Method}
 * only on request.
 *
 * @since 1.0
 * @see org.junit.platform.engine.support.descriptor.JavaMethodSource
 */
@API(Experimental)
public class JavaMethodSelector implements DiscoverySelector {

	private Class<?> javaClass;
	private final String className;
	private Method javaMethod;
	private final String methodName;
	private final String methodParameterTypes;

	JavaMethodSelector(String className, String methodName) {
		this(className, methodName, null);
	}

	JavaMethodSelector(String className, String methodName, String methodParameterTypes) {
		this.className = className;
		this.methodName = methodName;
		this.methodParameterTypes = methodParameterTypes;
	}

	JavaMethodSelector(Class<?> javaClass, String methodName) {
		this(javaClass, methodName, null);
	}

	public JavaMethodSelector(Class<?> javaClass, String methodName, String methodParameterTypes) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.methodName = methodName;
		this.methodParameterTypes = methodParameterTypes;
	}

	JavaMethodSelector(Class<?> javaClass, Method method) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.javaMethod = method;
		this.methodName = method.getName();
		this.methodParameterTypes = null;
	}

	/**
	 * Get the selected {@link Class} name.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Get the Java {@link Class} in which the selected {@linkplain #getJavaMethod
	 * javaMethod} is declared, or a subclass thereof.
	 *
	 * @see #getJavaMethod()
	 */
	public Class<?> getJavaClass() {
		lazyLoadJavaClassAndMethod();
		return this.javaClass;
	}

	/**
	 * Get the selected {@link Method} name.
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * Get the parameters types for the selected {@link Method} as a
	 * {@link String}. Note: The parameters are not provided as a list. This
	 * is by design to allow this generic class to be used by other JVM
	 * related languages.
	 */
	public String getMethodParameterTypes() {
		return this.methodParameterTypes;
	}

	/**
	 * Get the selected Java {@link Method}.
	 *
	 * @see #getJavaClass()
	 */
	public Method getJavaMethod() {
		lazyLoadJavaClassAndMethod();
		return this.javaMethod;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("className", this.className)
				.append("methodName", this.methodName)
				.append("methodParameterTypes", this.methodParameterTypes)
				.toString();
		// @formatter:on
	}

	private void lazyLoadJavaClassAndMethod() {
		if (this.javaClass == null) {
			this.javaClass = ReflectionUtils.loadClass(this.className).orElseThrow(
				() -> new PreconditionViolationException("Could not load class with name: " + this.className));
		}

		if (this.javaMethod == null) {
			if (StringUtils.isNotBlank(this.methodParameterTypes)) {
				this.javaMethod = ReflectionUtils.findMethod(this.javaClass, this.methodName,
					this.methodParameterTypes).orElseThrow(() -> new PreconditionViolationException(
						String.format("Could not find method with name [%s] and parameter types [%s] in class [%s].",
							this.methodName, this.methodParameterTypes, this.javaClass.getName())));
			}
			else {
				this.javaMethod = ReflectionUtils.findMethod(this.javaClass, this.methodName).orElseThrow(
					() -> new PreconditionViolationException(
						String.format("Could not find method with name [%s] in class [%s].", this.methodName,
							this.javaClass.getName())));
			}
		}
	}
}
