/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.util.io;

/**
 * Supplies values for macro substitution
 */
public interface MacroSupplier {
	/**
	 * @param key
	 * @return value if key exists or null if not
	 */
	String get(String key);
}
