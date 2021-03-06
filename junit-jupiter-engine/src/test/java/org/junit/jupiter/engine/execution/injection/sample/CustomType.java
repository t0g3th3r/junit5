/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution.injection.sample;

import java.util.Date;

/**
 * @since 5.0
 */
public class CustomType {

	private final Date date = new Date();

	@Override
	public String toString() {
		return "CustomType: " + this.date;
	}

}
