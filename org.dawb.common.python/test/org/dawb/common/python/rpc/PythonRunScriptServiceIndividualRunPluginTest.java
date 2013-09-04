package org.dawb.common.python.rpc;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

/** This concrete test runs each test with its own service instance. */
public class PythonRunScriptServiceIndividualRunPluginTest extends
		AbstractPythonRunScriptServicePluginTest {

	private AnalysisRpcPythonServiceManual service;
	private PythonRunScriptService runScript;

	@Override
	public Map<String, Object> runScript(String scriptFullPath,
			Map<String, ?> data) throws AnalysisRpcException {
		return runScript.runScript(scriptFullPath, data);
	}

	@Override
	public Map<String, Object> runScript(String scriptFullPath,
			Map<String, ?> data, String funcName)
			throws AnalysisRpcException {
		return runScript.runScript(scriptFullPath, data, funcName);
	}

	@Before
	public void before() throws AnalysisRpcException, IOException,
			CoreException {
		service = new AnalysisRpcPythonServiceManual();
		runScript = new PythonRunScriptService(service);
	}

	@After
	public void after() {
		service.stop();
	}
}