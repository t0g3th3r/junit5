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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Method} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on methods.
 * <p>
 * If a Java {@link Method} is provided, the selector will return this
 * {@link Method} and its method name accordingly. If the selector was
 * created with a {@link Class} and {@link Method} name, a {@link Class}
 * name and {@link Method} name, or only a full qualified {@link Method}
 * name, it will tries to lazy load the {@link Class} and {@link Method}
 * only on request. This way, this selector may also be used for non Java
 * methods, e.g. with Spock Specifications.
 *
 * @see org.junit.platform.engine.support.descriptor.JavaMethodSource
 * @since 1.0
 */
@API(Experimental)
public class MethodSelector implements DiscoverySelector {

	private static Pattern methodNameWithParametersPattern = Pattern.compile("([^(]+)\\(([^)]*)\\)");

	private Class<?> javaClass;
	private final String className;
	private Method javaMethod;
	private final String methodName;

	MethodSelector(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	MethodSelector(Class<?> javaClass, String methodName) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.methodName = methodName;
	}

	MethodSelector(Class<?> javaClass, Method method) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.javaMethod = method;
		this.methodName = method.getName();
	}

	/**
	 * Get the selected {@link Class} name.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Get the selected Java {@link Class}. It might throw an
	 * {@link PreconditionViolationException}, if the method selector is used
	 * for non Java {@link Class}, e.g. spock specifications.
	 *
	 * @throws PreconditionViolationException if there is no such Java {@link Class}
	 * @see #getJavaMethod()
	 */
	public Class<?> getJavaClass() throws PreconditionViolationException {
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
	 * Get the selected Java {@link Method}. It might throw an
	 * {@link PreconditionViolationException}, if the method selector is used
	 * for non Java {@link Method}, e.g. spock specifications.
	 *
	 * @throws PreconditionViolationException if there is no such Java {@link Method}
	 * @see #getJavaClass()
	 */
	public Method getJavaMethod() throws PreconditionViolationException {
		lazyLoadJavaClassAndMethod();
		return this.javaMethod;
	}

	@Override
	public String toString() {
		// @formatter:off
        return new ToStringBuilder(this)
                .append("className", this.className)
                .append("methodName", this.methodName)
                .toString();
        // @formatter:on
	}

	private void lazyLoadJavaClassAndMethod() throws PreconditionViolationException {
		if (this.javaClass == null) {
			this.javaClass = ReflectionUtils.loadClass(this.className).orElseThrow(
				() -> new PreconditionViolationException("Could not load class with name: " + this.className));
		}

		if (this.javaMethod == null) {
			Matcher matcher = methodNameWithParametersPattern.matcher(this.methodName);
			if (matcher.matches()) {
				String methodNameOnly = matcher.group(1);
				String parameterTypes = matcher.group(2);
				this.javaMethod = ReflectionUtils.findMethod(this.javaClass, methodNameOnly,
					parameterTypes).orElseThrow(
						() -> new PreconditionViolationException(
							String.format("Could not find method with name [%s] in class [%s].", this.methodName,
								this.javaClass.getName())));
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
