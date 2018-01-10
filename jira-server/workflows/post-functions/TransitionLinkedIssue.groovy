/**
 * @user: Justin Alex Paramanandan
 * @company: ServiceRocket
 * @fileName: StatusSyncIBImplementation.groovy
 * @createdDate: 09-10-2017
 * @modifiedDate: 09-10-2017
 * @version: 0.1
 * @description: A script to transition a linked PMO SD request to IN PROGRESS when the current issue is PENDING IMPLEMENTATION.
 *               To be used in the IB project only.
 *
 **/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager

def destinationStatusId = 61 // the status ID for the IN PROGRESS transition in PMO SD

def issueService = ComponentAccessor.getIssueService()
def commentManager = ComponentAccessor.CommentManager
UserManager um = ComponentAccessor.UserManager

// get destination issue from current issue
def destinationIssue = ComponentAccessor.getIssueLinkManager().getLinkCollectionOverrideSecurity(issue)?.allIssues?.getAt(0)

// define the user to transition the linked PMO SD request
ApplicationUser user = um.getUserByName("pmo-sd-agent")

// Prepare our input for the transition
def issueInputParameters = issueService.newIssueInputParameters()

// Validate transitioning the linked issue to "In Progress"
def validationResult = issueService.validateTransition(user, destinationIssue.id, 61, issueInputParameters)
if (validationResult.isValid()) {
    // Perform the transition
    def issueResult = issueService.transition(user, validationResult)
    // add a comment to the linked PMO SD request
    commentManager.create(destinationIssue, user, "This request is now in progress.", true)
    if (! issueResult.isValid()) {
        log.warn("Failed to transition request ${destinationIssue.key}, errors: ${issueResult.errorCollection}")
    }
} else {
    log.warn("Could not transition request ${destinationIssue.key}, errors: ${validationResult.errorCollection}")
}
