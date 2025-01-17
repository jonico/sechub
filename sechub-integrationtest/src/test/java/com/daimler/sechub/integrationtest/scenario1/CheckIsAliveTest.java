// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario1;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;

public class CheckIsAliveTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

	/* +-----------------------------------------------------------------------+ */
	/* +............................ Get server version .......................+ */
	/* +-----------------------------------------------------------------------+ */

	@Test
	public void get_server_version() {
		/* execute */
		boolean alive= as(ANONYMOUS).getIsAlive();

		/* test*/
		assertTrue(alive);
	}


}
