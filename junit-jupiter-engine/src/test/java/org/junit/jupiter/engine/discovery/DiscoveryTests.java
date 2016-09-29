/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Test correct test discovery in simple test classes for the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
public class DiscoveryTests extends AbstractJupiterTestEngineTests {

	@Test
	public void discoverTestClass() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(LocalTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(7, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void doNotDiscoverAbstractTestClass() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(AbstractTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(0, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverMethodByUniqueId() {
		LauncherDiscoveryRequest request = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test1()"))).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverMethodByUniqueIdForOverloadedMethod() {
		LauncherDiscoveryRequest request = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test4()"))).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverMethodByUniqueIdForOverloadedMethodVariantThatAcceptsArguments() {
		LauncherDiscoveryRequest request = request().selectors(selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(
			LocalTestCase.class, "test4(" + TestInfo.class.getName() + ")"))).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverMethodByMethodReference() throws NoSuchMethodException {
		Method testMethod = LocalTestCase.class.getDeclaredMethod("test3", new Class<?>[0]);

		LauncherDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, testMethod)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void discoverCompositeSpec() {
		LauncherDiscoveryRequest spec = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test2()")),
			selectClass(LocalTestCase.class)).build();

		TestDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(7, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	// -------------------------------------------------------------------

	private static abstract class AbstractTestCase {

		@Test
		void abstractTest() {

		}
	}

	private static class LocalTestCase {

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}

		@Test
		void test3() {
		}

		@Test
		void test4() {
		}

		@Test
		void test4(TestInfo testInfo) {
		}

		@CustomTestAnnotation
		void customTestAnnotation() {
			/* no-op */
		}

	}

	@Test
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}
}
