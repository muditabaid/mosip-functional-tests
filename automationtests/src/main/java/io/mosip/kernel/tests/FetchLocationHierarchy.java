

package io.mosip.kernel.tests;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Verify;

import io.mosip.kernel.service.ApplicationLibrary;
import io.mosip.kernel.service.AssertKernel;
import io.mosip.kernel.util.CommonLibrary;
import io.mosip.kernel.util.KernelAuthentication;
import io.mosip.kernel.util.KernelDataBaseAccess;
import io.mosip.kernel.util.TestCaseReader;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

/**
 * @author Arjun chandramohan
 *
 */
public class FetchLocationHierarchy extends BaseTestCase implements ITest {
	FetchLocationHierarchy() {
		super();
	}

	private static Logger logger = Logger.getLogger(FetchLocationHierarchy.class);
	private final String moduleName = "kernel";
	private final String apiName = "fetchLocationHierarchy";
	public CommonLibrary lib=new CommonLibrary();
	private final Map<String, String> props = lib.readProperty("Kernel");
	private final String FetchLocationHierarchy_URI_withlangCode = props.get("FetchLocationHierarchy_URI_withlangCode").toString();
	private final String FetchLocationHierarchy_URI_locationcode = props.get("FetchLocationHierarchy_URI_locationcode").toString();
	private final String FetchLocationHierarchy_URI_hierarchyname = props.get("FetchLocationHierarchy_URI_hierarchyname").toString();
	protected String testCaseName = "";
	SoftAssert softAssert = new SoftAssert();
	boolean status = false;
	String finalStatus = "";
	public JSONArray arr = new JSONArray();
	Response response = null;
	JSONObject responseObject = null;
	private AssertKernel assertions = new AssertKernel();
	private ApplicationLibrary applicationLibrary = new ApplicationLibrary();
	KernelAuthentication auth=new KernelAuthentication();

	/**
	 * method to set the test case name to the report
	 * 
	 * @param method
	 * @param testdata
	 * @param ctx
	 */
	@BeforeMethod(alwaysRun=true)
	public void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) throws Exception {
		String object = (String) testdata[0];
		testCaseName = moduleName+"_"+apiName+"_"+object.toString();
		if(!lib.isValidToken(adminCookie))
			adminCookie=auth.getAuthForAdmin();
	}

	/**
	 * This data provider will return a test case name
	 * 
	 * @param context
	 * @return test case name as object
	 */
	@DataProvider(name = "fetchData")
	public Object[][] readData(ITestContext context)
			throws JsonParseException, JsonMappingException, IOException, ParseException {		
		return new TestCaseReader().readTestCases(moduleName + "/" + apiName, testLevel);
	}

	/**
	 * This fetch the value of the data provider and run for each test case
	 * 
	 * @param fileName
	 * @param object
	 * @throws ParseException 
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test(dataProvider = "fetchData", alwaysRun = true)
	public void fetchLocationHierarchy(String testcaseName) throws ParseException{
		logger.info("Test Case Name:" + testcaseName);

		// getting request and expected response jsondata from json files.
		JSONObject objectDataArray[] = new TestCaseReader().readRequestResponseJson(moduleName, apiName, testcaseName);

		JSONObject objectData = objectDataArray[0];
		responseObject = objectDataArray[1];
				if (objectData.containsKey("hierarchyname"))
					response = applicationLibrary.getWithPathParam(FetchLocationHierarchy_URI_hierarchyname, objectData,adminCookie);
				else if (objectData.containsKey("locationcode"))
					response = applicationLibrary.getWithPathParam(FetchLocationHierarchy_URI_locationcode, objectData,adminCookie);
				else
					response = applicationLibrary.getWithPathParam(FetchLocationHierarchy_URI_withlangCode, objectData,adminCookie);

				//This method is for checking the authentication is pass or fail in rest services
				new CommonLibrary().responseAuthValidation(response);		
				
				if (testcaseName.toLowerCase().contains("smoke")) {

					// fetching json object from response
					JSONObject responseJson = (JSONObject) ((JSONObject) new JSONParser().parse(response.asString())).get("response");
					if (responseJson == null || !responseJson.containsKey("locations"))
						Assert.assertTrue(false, "Response does not contain locations");
					
					String queryToExecute = null;
					// list to validate existance of attributes in response objects
					List<String> attributesToValidateExistance = new ArrayList<String>();
					// key value of the attributes passed to fetch the data, should be same in all
					// obtained objects
					HashMap<String, String> passedAttributesToFetch = new HashMap<String, String>();;
					
					if (objectData.containsKey("hierarchyname"))
					{
					  queryToExecute = "select count(*) from master.location where is_active=true and hierarchy_level_name = '"+objectData.get("hierarchyname")+"'";
					  
						attributesToValidateExistance.add("code");
						attributesToValidateExistance.add("name");
						attributesToValidateExistance.add("hierarchyName");
						attributesToValidateExistance.add("hierarchyLevel");
						attributesToValidateExistance.add("parentLocCode");
						attributesToValidateExistance.add("langCode");
						
						passedAttributesToFetch.put("hierarchyName", objectData.get("hierarchyname").toString());
					}
					else
					{
					  queryToExecute = "select count(*) from master.loc_hierarchy_list where is_active=true and lang_code = '"+objectData.get("langcode")+"'";
					  
					// list to validate existance of attributes in response objects
					  	attributesToValidateExistance = new ArrayList<String>();
						attributesToValidateExistance.add("locationHierarchylevel");
						attributesToValidateExistance.add("locationHierarchyName");
						// key value of the attributes passed to fetch the data, should be same in all
						// obtained objects
						passedAttributesToFetch = new HashMap<String, String>();
					}
					
					
					long obtainedObjectsCount = new KernelDataBaseAccess().validateDBCount(queryToExecute,"masterdata");

					// fetching json array of objects from response
					JSONArray responseArrayFromGet = (JSONArray) responseJson.get("locations");
					logger.info("===Dbcount===" + obtainedObjectsCount + "===Get-count===" + responseArrayFromGet.size());

					// validating number of objects obtained form db and from get request
					if (responseArrayFromGet.size() == obtainedObjectsCount) {
						status = AssertKernel.validator(responseArrayFromGet, attributesToValidateExistance,
								passedAttributesToFetch);
					} else
						status = false;

				}

				else {

					// add parameters to remove in response before comparison like time stamp
					ArrayList<String> listOfElementToRemove = new ArrayList<String>();
					listOfElementToRemove.add("responsetime");
					
					status = assertions.assertKernel(response, responseObject, listOfElementToRemove);
				}	
				
		
		if (!status) {
			logger.info("Response from the Request == "+response.asString());
		}
		Verify.verify(status);
		softAssert.assertAll();
	}

	@Override
	public String getTestName() {
		return this.testCaseName;
	}

	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		try {
			Field method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}

}

