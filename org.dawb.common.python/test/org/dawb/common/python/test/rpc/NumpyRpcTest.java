/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.python.test.rpc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.python.rpc.PythonService;
import org.dawb.common.util.eclipse.BundleUtils;
import org.dawb.common.util.test.TestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;


public class NumpyRpcTest {
	

	
	private static PythonService service;

	@BeforeClass
	public static void start() throws Exception {
		
		service = PythonService.openConnection("python");
	    
	}

	@AfterClass
	public static void stop() {
		if (service!=null) service.stop();
	}
	
	/**
	 * Test a script which creates a data set called x and multiples
	 * it by two
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSimpleScript() throws Exception {
				
		if (!service.isRunning()) {
			throw new Exception("No server going!");
		}
		
		final AbstractDataset             set  = AbstractDataset.arange(0,100,1,AbstractDataset.INT32);
		final Map<String,AbstractDataset> data = new HashMap<String, AbstractDataset>();
		data.put("x", set);
		
		final String scriptPath = BundleUtils.getBundleLocation("org.dawb.common.python").getAbsolutePath()+"/test/org/dawb/common/python/test/rpc/2x.py";	
		
		final Map<String,? extends Object> out  = service.runScript(scriptPath, data, Arrays.asList(new String[]{"x","y"})); // Calls the method 'run' in the script with the arguments

		final AbstractDataset y = (AbstractDataset)out.get("y");
		if (y.getDouble(1)!=2) throw new Exception("Testing x*2 failed!");
		
		// Got to here, then we have passed.
		System.out.println("Completed multiplication of x by 2 and returning it as y!");
	}
	
	@Test
	public void testLog() throws Exception {
				
		if (!service.isRunning()) {
			throw new Exception("No server going!");
		}
		
		final AbstractDataset             q  = AbstractDataset.arange(1,100,1,AbstractDataset.INT32);
		final AbstractDataset             p  = AbstractDataset.arange(201,300,1,AbstractDataset.INT32);
		final Map<String,AbstractDataset> data = new HashMap<String, AbstractDataset>();
		data.put("p", p);
		data.put("q", q);
		
		final String scriptPath = BundleUtils.getBundleLocation("org.dawb.common.python").getAbsolutePath()+"/test/org/dawb/common/python/test/rpc/log.py";	
		
		final Map<String,? extends Object> out  = service.runScript(scriptPath, data, Arrays.asList(new String[]{"res"})); // Calls the method 'run' in the script with the arguments

		final AbstractDataset res = (AbstractDataset)out.get("res");
		
		// Got to here, then we have passed.
		final double val = res.getDouble(1);
		if (val>4.7 || val <4.5) throw new Exception("Testing log failed!");
		System.out.println("Completed test of log in numpy!");
	}

	
	@Test
	public void testRealData() throws Exception {
				
		if (!service.isRunning()) {
			throw new Exception("No server going!");
		}
		
		String path = TestUtils.getAbsolutePath(org.dawb.common.python.Activator.getDefault().getBundle(), 
                            "test/org/dawb/common/python/test/billeA_4201_EF_XRD_5998.edf");				
		final AbstractDataset             set  = LoaderFactory.getData(path).getDataset(0);
		final Map<String,AbstractDataset> data = new HashMap<String, AbstractDataset>();
		data.put("x", set);
		
		final String scriptPath = BundleUtils.getBundleLocation("org.dawb.common.python").getAbsolutePath()+"/test/org/dawb/common/python/test/rpc/2x.py";	
		
		final Map<String,? extends Object> out  = service.runScript(scriptPath, data, Arrays.asList(new String[]{"y"})); // Calls the method 'run' in the script with the arguments

		final AbstractDataset y = (AbstractDataset)out.get("y");
		final double val = y.getDouble(1);
		if (val!=202d) throw new Exception("Testing x*2 failed!");
		
		// Got to here, then we have passed.
		System.out.println("Completed multiplication of real data by 2 and returning it as y!");
	}

}
