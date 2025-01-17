// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.notification.superadmin.InformAdminsThatSchedulerJobProcessingHasBeenDisabledService;
import com.daimler.sechub.domain.notification.superadmin.InformAdminsThatSchedulerJobProcessingHasBeenEnabledService;
import com.daimler.sechub.domain.notification.superadmin.InformAdminsThatUserBecomesAdminNotificationService;
import com.daimler.sechub.domain.notification.superadmin.InformAdminsThatUserNoLongerAdminNotificationService;
import com.daimler.sechub.domain.notification.user.InformUserThatUserBecomesAdminNotificationService;
import com.daimler.sechub.domain.notification.user.InformUserThatUserNoLongerAdminNotificationService;
import com.daimler.sechub.domain.notification.user.NewAPITokenAppliedUserNotificationService;
import com.daimler.sechub.domain.notification.user.NewApiTokenRequestedUserNotificationService;
import com.daimler.sechub.domain.notification.user.SignUpRequestedAdminNotificationService;
import com.daimler.sechub.domain.notification.user.UserDeletedNotificationService;
import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;

@Component
public class NotificationMessageHandler implements AsynchronMessageHandler {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationMessageHandler.class);

	@Autowired
	UserDeletedNotificationService userDeletedNotificationService;

	@Autowired
	NewAPITokenAppliedUserNotificationService newAPITokenAppliedUserNotificationService;

	@Autowired
	NewApiTokenRequestedUserNotificationService newApiTokenRequestedUserNotificationService;

	@Autowired
	SignUpRequestedAdminNotificationService signupRequestedAdminNotificationService;

	@Autowired
	InformUserThatUserBecomesAdminNotificationService informUserThatUserBecomesAdminNotificationService;

	@Autowired
	InformAdminsThatUserBecomesAdminNotificationService informAdminsThatUserBecomesAdminNotificationService;

	@Autowired
	InformUserThatUserNoLongerAdminNotificationService informUserThatUserNoLongerAdminNotificationService;

	@Autowired
	InformAdminsThatUserNoLongerAdminNotificationService informAdminsThatUserNoLongerAdminNotificationService;

	@Autowired
	InformAdminsThatSchedulerJobProcessingHasBeenDisabledService informAdminSchedulerDisabledService;

	@Autowired
	InformAdminsThatSchedulerJobProcessingHasBeenEnabledService informAdminSchedulerEnabledService;


	@Override
	public void receiveAsyncMessage(DomainMessage request) {
		MessageID messageId = request.getMessageId();

		LOG.debug("received domain request: {}", request);

		switch (messageId) {
		case USER_NEW_API_TOKEN_REQUESTED:
			handlNewAPITokenRequested(request.get(MessageDataKeys.USER_ONE_TIME_TOKEN_INFO));
			break;
		case USER_API_TOKEN_CHANGED:
			handleUserApiTokenChanged(request.get(MessageDataKeys.USER_API_TOKEN_DATA));
			break;
		case USER_DELETED:
			handleUserDeleted(request.get(MessageDataKeys.USER_DELETE_DATA));
			break;
		case USER_SIGNUP_REQUESTED:
			handleSignupRequested(request.get(MessageDataKeys.USER_SIGNUP_DATA));
			break;
		case USER_BECOMES_SUPERADMIN:
			handleUserBecomesSuperAdmin(request.get(MessageDataKeys.USER_CONTACT_DATA),request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
			break;
		case USER_NO_LONGER_SUPERADMIN:
			handleUserNoLongerSuperAdmin(request.get(MessageDataKeys.USER_CONTACT_DATA),request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
			break;
		case SCHEDULER_JOB_PROCESSING_DISABLED:
			handleSchedulerJobProcessingDisabled(request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
			break;
		case SCHEDULER_JOB_PROCESSING_ENABLED:
			handleSchedulerJobProcessingEnabled(request.get(MessageDataKeys.ENVIRONMENT_BASE_URL));
			break;
		default:
			throw new IllegalStateException("unhandled message id:" + messageId);
		}
	}

	@IsReceivingAsyncMessage(MessageID.SCHEDULER_JOB_PROCESSING_DISABLED)
	private void handleSchedulerJobProcessingDisabled(String envBaseUrl) {
		informAdminSchedulerDisabledService.notify(envBaseUrl);
	}

	@IsReceivingAsyncMessage(MessageID.SCHEDULER_JOB_PROCESSING_ENABLED)
	private void handleSchedulerJobProcessingEnabled(String envBaseUrl) {
		informAdminSchedulerEnabledService.notify(envBaseUrl);
	}

	@IsReceivingAsyncMessage(MessageID.USER_NO_LONGER_SUPERADMIN)
	private void handleUserNoLongerSuperAdmin(UserMessage userMessage, String baseUrl) {
		informUserThatUserNoLongerAdminNotificationService.notify(userMessage, baseUrl);
		informAdminsThatUserNoLongerAdminNotificationService.notify(userMessage, baseUrl);
	}

	@IsReceivingAsyncMessage(MessageID.USER_BECOMES_SUPERADMIN)
	private void handleUserBecomesSuperAdmin(UserMessage userMessage, String baseURL) {
		informUserThatUserBecomesAdminNotificationService.notify(userMessage, baseURL);
		informAdminsThatUserBecomesAdminNotificationService.notify(userMessage, baseURL);
	}

	@IsReceivingAsyncMessage(MessageID.USER_DELETED)
	private void handleUserDeleted(UserMessage userMessage) {
		userDeletedNotificationService.notify(userMessage);
	}

	@IsReceivingAsyncMessage(MessageID.USER_API_TOKEN_CHANGED)
	private void handleUserApiTokenChanged(UserMessage userMessage) {
		newAPITokenAppliedUserNotificationService.notify(userMessage);
	}

	@IsReceivingAsyncMessage(MessageID.USER_NEW_API_TOKEN_REQUESTED)
	private void handlNewAPITokenRequested(UserMessage userMessage) {
		newApiTokenRequestedUserNotificationService.notify(userMessage);
	}

	@IsReceivingAsyncMessage(MessageID.USER_SIGNUP_REQUESTED)
	private void handleSignupRequested(UserMessage userMessage) {
		signupRequestedAdminNotificationService.notify(userMessage);
	}

}
