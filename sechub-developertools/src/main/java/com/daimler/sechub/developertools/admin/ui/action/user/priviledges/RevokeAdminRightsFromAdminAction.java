// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.user.priviledges;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class RevokeAdminRightsFromAdminAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public RevokeAdminRightsFromAdminAction(UIContext context) {
		super("Revoke admin rights from admin",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> userToSignup = getUserInput("Please enter userid who will have no longer admin rights",InputCacheIdentifier.USERNAME);
		if (!userToSignup.isPresent()) {
			return;
		}
		String data = getContext().getAdministration().revokeAddminRightsFrom(userToSignup.get());
		output(data);
	}

}