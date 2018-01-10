/**
  * @name: TransitionLinkedParentIssueWithValidation.groovy
  * @author jalex
  * @version 1.0
  *
  * A Groovy script to transition a linked "parent" issue when all linked issues are resolved.
  * This is useful when there are linked issues which blocks the completion of the linked parent issue,
  * and automatically transition it when all is complete.
  *
  * An example of when this script might be useful is when we are managing requests for system access in
  * Service Desk. An example would be:
  * 
  * - TEST-1: Request for Access - Justin Alex (linked parent issue)
  * -- TEST-2: Justin Alex - JIRA System Access (linked issue to TEST-1)
  * -- TEST-3: Justin Alex - Confluence System Access (linked issue to TEST-1)
  * -- TEST-4: Justin Alex - Bitbucket System Access (linked issue to TEST-1)
  *
  * TEST-1 can only be resolved when TEST-2, TEST-3 and TEST-4 are resolved. This script will do this automatically.
  */

import org.apache.log4j.Category
import com.atlassian.jira.ComponentManager 
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.project.Project
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.IssueInputParameters

// Global Variables
def outLinkName = "requests"
def linkedStatus ="In Progress"
def inLinkName = "is requested by"
def linkedIssueStatus = "Resolved"
def nextTransitionID = 761
def testIssueKey = "TEST-3"

def Category log = Category.getInstance("com.onresolve.jira.groovy.TransitionToDevComplete")

ApplicationUser currentUserObj = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

def issueManager = ComponentAccessor.getIssueManager()
IssueLinkManager linkMgr = ComponentAccessor.getIssueLinkManager()

// FOR TESTING IN SCRIPT CONSOLE
Issue issue = issueManager.getIssueObject(testIssueKey);

log.debug "Current issue: ${issue.getKey()}"
log.debug "Looking for Links..."

// Iterate through the dev issue's links
def inwardLinks = linkMgr.getInwardLinks(issue.id)

if (inwardLinks != null) {
    for (IssueLink inLink : inwardLinks) {
        def issueLinkTypeName = inLink.issueLinkType.getOutward()
        def linkedParentIssue = inLink.getSourceObject()
        def linkedParentIssueKey = linkedParentIssue.getKey()
        def linkedParentIssueID = linkedParentIssue.getId()
        def linkedParentIssueStatus = linkedParentIssue.getStatus().getName()
        
        log.debug "Linked Source Issue: ${linkedParentIssueKey}"
        log.debug "Linked Source Issue ID: ${linkedParentIssueID}"
        log.debug "Linked Source Issue Link Name: ${issueLinkTypeName}"
        log.debug "Current Status of Linked Source Issue: ${linkedParentIssueStatus}"
 
        // look for a link that blocks an In Dev Tech Support issue
        if (issueLinkTypeName == outLinkName && linkedParentIssueStatus == linkedStatus) {
            log.debug "Issue ${issue.getKey()} blocks issue ${linkedParentIssueKey}"
            def allClosed = true
     
            // go to tech support issue and see if it has other open blockers
            def outwardLinks = linkMgr.getOutwardLinks(linkedParentIssueID)
            log.debug "Number of Outward Links from ${linkedParentIssueID}: ${outwardLinks.size()}"

            if (outwardLinks != null) {
                def iteration = 0;
                for (IssueLink outLink : outwardLinks ) {
                    iteration ++
                    def linkedIssue = outLink.getDestinationObject()
                    def linkedIssueKey = linkedIssue.getKey()
                    def linkedIssueID = linkedIssue.getId()
                    def linkedIssueLinkTypeName = outLink.issueLinkType.getInward()
                    def linkedIssueStatusName = linkedIssue.getStatus().name
                    log.debug "#${iteration} - Linked Destination Issue: ${linkedIssueKey}"
                    log.debug "#${iteration} - Linked Destination Issue ID: ${linkedIssueID}"
                    log.debug "#${iteration} - Linked Destination Issue Link Name: ${linkedIssueLinkTypeName}"
                    log.debug "#${iteration} - Current Status of Linked Destination Issue: ${linkedIssueStatusName}"

                    if (issue.getKey() != linkedIssueKey  && linkedIssueLinkTypeName == inLinkName) {
                        if (linkedIssueStatusName == linkedIssueStatus) {
                            log.debug "Issue ${linkedIssueKey} is resolved"
                        } else {
                            log.debug "Issue ${linkedIssueKey} is still open"
                            allClosed = false
                        }
                    }
                }
            }

            log.debug ("All issues resolved? " + allClosed)
            
            // if no other blockers remain open, try to transition the issue
            if (allClosed) {
                log.debug "Transition issue ${linkedParentIssueKey}"
                IssueService issueService = ComponentAccessor.getIssueService()
                IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
                issueInputParameters.setResolutionId("10000");
                TransitionValidationResult validationResult = issueService.validateTransition(currentUserObj, linkedParentIssueID, nextTransitionID, issueInputParameters)
                
                if (validationResult.isValid()) {
                // Transition linked issue to status = "Reslve"
                    issueService.transition(currentUserObj, validationResult)
                    log.debug "Transitioned"
                } else {
                    Collection<String> errors = validationResult.getErrorCollection().getErrorMessages()
                    for (errmsg in errors) {
                        log.error "[ERROR] - Error message: ${errmsg}"
                    }
                }
            }
        }
    }
}
