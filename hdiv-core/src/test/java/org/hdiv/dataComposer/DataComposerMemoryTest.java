/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hdiv.dataComposer;

import org.hdiv.AbstractHDIVTestCase;
import org.hdiv.session.ISession;
import org.hdiv.state.IPage;
import org.hdiv.state.IState;
import org.hdiv.state.StateUtil;
import org.hdiv.util.HDIVUtil;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for the <code>org.hdiv.composer.DataComposerMemory</code> class.
 * 
 * @author Gorka Vicente
 */
public class DataComposerMemoryTest extends AbstractHDIVTestCase {

	private DataComposerFactory dataComposerFactory;

	private StateUtil stateUtil;

	private ISession session;

	/*
	 * @see TestCase#setUp()
	 */
	protected void onSetUp() throws Exception {

		this.dataComposerFactory = (DataComposerFactory) this.getApplicationContext().getBean("dataComposerFactory");
		this.stateUtil = (StateUtil) this.getApplicationContext().getBean("stateUtil");
		this.session = (ISession) this.getApplicationContext().getBean("sessionHDIV");
	}

	/**
	 * @see DataComposerMamory#compose(String, String, String, boolean)
	 */
	public void testComposeSimple() {

		IDataComposer dataComposer = this.dataComposerFactory.newInstance();

		dataComposer.startPage();
		dataComposer.beginRequest("test.do");

		boolean confidentiality = this.getConfig().getConfidentiality().booleanValue();

		// we add a multiple parameter that will be encoded as 0, 1, 2, ...
		String result = dataComposer.compose("action1", "parameter1", "2", false);
		String value = (!confidentiality) ? "2" : "0";
		assertTrue(value.equals(result));

		result = dataComposer.compose("action1", "parameter1", "2", false);
		value = (!confidentiality) ? "2" : "1";
		assertTrue(value.equals(result));

		result = dataComposer.compose("action1", "parameter1", "2", false);
		assertTrue("2".equals(result));

		result = dataComposer.compose("action1", "parameter2", "2", false);
		value = (!confidentiality) ? "2" : "0";
		assertTrue(value.equals(result));

		result = dataComposer.compose("action1", "parameter2", "2", false);
		value = (!confidentiality) ? "2" : "1";
		assertTrue(value.equals(result));
	}

	public void testComposeAndRestore() {

		IDataComposer dataComposer = this.dataComposerFactory.newInstance();

		dataComposer.startPage();
		dataComposer.beginRequest("test.do");
		dataComposer.compose("action1", "parameter1", "2", false);
		String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		IState state = this.stateUtil.restoreState(stateId);

		assertEquals("test.do", state.getAction());
	}

	public void testComposeExistingState() {
		MockHttpServletRequest request = (MockHttpServletRequest) HDIVUtil.getHttpServletRequest();

		IDataComposer dataComposer = this.dataComposerFactory.newInstance();

		dataComposer.startPage();
		dataComposer.beginRequest("test.do");
		dataComposer.compose("action1", "parameter1", "2", false);
		String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);
		request.addParameter("_PREVIOUS_HDIV_STATE_", stateId);

		//New request
		IState state = this.stateUtil.restoreState(stateId);
		IPage page = this.session.getPage(state.getPageId());
		dataComposer = this.dataComposerFactory.newInstance();
		dataComposer.startPage(page);
		dataComposer.beginRequest(state);
		dataComposer.compose("action1", "parameter1", "3", false);
		String stateId2 = dataComposer.endRequest();
		dataComposer.endPage();

		assertEquals(stateId, stateId2);
		IState state2 = this.stateUtil.restoreState(stateId2);
		assertEquals(state2.getParameter("parameter1").getCount(), 2);
		assertTrue(state2.getParameter("parameter1").existValue("3"));
	}

}
