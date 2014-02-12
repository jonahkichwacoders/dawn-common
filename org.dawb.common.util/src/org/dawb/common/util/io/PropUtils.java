/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Properties;

public final class PropUtils {

	////////////////////////////////////////////////////////////////////////
	public final static Properties loadProperties(final String path) throws IOException {
		final File file                  = new File(path);
		return PropUtils.loadProperties(file);
	}
	////////////////////////////////////////////////////////////////////////
	public final static Properties loadProperties(final File file) throws IOException {   	
		if (!file.exists()) return new Properties();
		return loadProperties(new FileInputStream(file));
	}

	public final static Properties loadProperties(final InputStream stream) throws IOException {   	
		
		final Properties fileProps       = new Properties();
		try {
			final BufferedInputStream in     = new BufferedInputStream(stream);
			fileProps.load(in);
		} finally {
			stream.close();
		}
		return fileProps;
	}

	////////////////////////////////////////////////////////////////////////
	public final static Properties storeProperties(final Properties props,
			final String path) throws IOException{
		final File file                  = new File(path);
		return PropUtils.storeProperties(props, file);
	}

	////////////////////////////////////////////////////////////////////////
	public final static Properties storeProperties(final Properties props, final File file) throws IOException {

		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if (!file.exists()) file.createNewFile();
		BufferedOutputStream out = null;
		try {
			final FileOutputStream os        = new FileOutputStream(file);
			out   = new BufferedOutputStream(os);
			props.store(out, "DAWB Properties. Please do not edit this file.");
		} finally {
			IOUtils.close(out,"Storing properties for file "+IOUtils.fileInfo(file));
		}
		return props;
	}

	public final static Properties storeKeys(final Properties props,
			final File file) throws IOException{

		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if (!file.exists()) file.createNewFile();

		PrintWriter out = null;
		try {
			out   = new PrintWriter(new FileWriter(file));
			final Iterator<?> it = props.keySet().iterator();
			while(it.hasNext()) out.println((String)it.next());
		} finally {
			IOUtils.close(out,"Storing keys for file "+IOUtils.fileInfo(file));
		}
		return props;
	}

	public static final Properties loadProperties(final HttpURLConnection connection) throws IOException {
		BufferedInputStream in    = null;
		final Properties urlProps = new Properties();
		try {
			final InputStream stream = connection.getInputStream();
			in     = new BufferedInputStream(stream);
			urlProps.load(in);
		} finally {
			if (in!=null) {
				IOUtils.close(in,"Loading properties from URL");
				connection.disconnect();  // Must do for HTTP because servers have limited connections 
			}
		}                    	
		return urlProps;
	}
	
	
	public static String getPropertiesAsString(Properties props) throws Exception {
		
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
		    props.store(out, " DAWB output properties");
		} finally {
			out.close();
		}
		
		return out.toString("8859_1");
	}
	
	/**
	 * String to be parsed to properties. In the form of key=value pairs
	 * separated by semi colons. You may not use the string = or ; in the 
	 * keys or values. Keys and values are trimmed so extra spaces will be
	 * ignored.
	 * 
	 * @param secondId
	 * @return map of values extracted from the 
	 */
	public static Properties parseString(String properties) {
		
		if (properties==null) return new Properties();
		Properties props = new Properties();
		final String[] split = properties.split(";");
		for (String line : split) {
			final String[] kv = line.split("=");
			props.setProperty(kv[0].trim(), kv[1].trim());
		}
		return props;
	}
}