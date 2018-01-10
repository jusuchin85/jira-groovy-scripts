/**
 * @user: Justin Alex Paramanandan
 * @company: ServiceRocket
 * @fileName: CreateIssueAndLink.groovy
 * @createdDate: 08-31-2017
 * @modifiedDate: 08-31-2017
 * @version: 0.1
 * @description: This script automatically creates issues in either the IB, IDG or EDU Jira Software projects,
 *               based on the criteria selected in PMO Service Desk. Upon creation, the script will then link
 *               the destination issue back to its source. The relationship between source and destination is 1:1.
 *
 *               This script should be used as a postfunction in a workflow transition. Upon executing that transition,
 *               the script will then automatically perform the process.
 **/

 import com.atlassian.jira.component.ComponentAccessor
 import com.atlassian.jira.issue.CustomFieldManager
 import com.atlassian.jira.issue.fields.CustomField
 import com.atlassian.jira.issue.IssueFactory
 import com.atlassian.jira.issue.IssueManager
 import com.atlassian.jira.issue.Issue
 import com.atlassian.jira.issue.link.IssueLink
 import com.atlassian.jira.issue.MutableIssue
 import com.atlassian.jira.issue.link.IssueLinkManager
 import com.atlassian.jira.project.ProjectManager
 import com.atlassian.jira.user.ApplicationUser
 import com.atlassian.jira.util.ImportUtils
 import com.atlassian.crowd.embedded.api.User
 import com.opensymphony.workflow.WorkflowContext
 import org.apache.log4j.Category
 import com.atlassian.jira.user.util.UserManager
 import com.atlassian.jira.project.Project

// Global variables
def requestTypeFieldName = "Request Type"

// Logging variables
Category infoLog = Category.getInstance("com.onresolve.jira.groovy.PostFunction")
infoLog.setLevel(org.apache.log4j.Level.INFO)

// Configurable section
def bugIssueTypeID = "10008"
def taskIssueTypeID = "10105"
def linkName = "Relates"
def linkNameID = "10003"

IssueManager issueMgr = ComponentAccessor.issueManager
ProjectManager projectMgr = ComponentAccessor.projectManager

UserManager userManager  = ComponentAccessor.getUserManager()
ApplicationUser currentUser

CustomFieldManager customFieldManager = ComponentAccessor.customFieldManager
CustomField requestTypeCF = customFieldManager.getCustomFieldObjectByName(requestTypeFieldName)
String requestTypeCFValue = requestTypeCF.getValue(issue).toString()

def wasIndexing = ImportUtils.indexIssues
ImportUtils.indexIssues = true
IssueFactory issueFactory = ComponentAccessor.issueFactory
MutableIssue sourceIssue = issue
MutableIssue newIssue = issueFactory.getIssue()

// Create JIRA Software issues based on issue type and Service Desk request type
if (issue.getIssueType().getName().equalsIgnoreCase("Service Request")) {
    newIssue = createNewIssue (projectMgr, "IDG", sourceIssue, newIssue, currentUser, bugIssueTypeID, "")
} /*else if (issue.getIssueType().getName().equalsIgnoreCase("Research")) {
    Project projectObj = projectMgr.getProjectObjByKey("IDG")
    currentUserObj = projectObj.getProjectLead()
    infoLog.info "CURRENT USER: ${currentUserObj.getName()}"
    newissue.setSummary (issue.getSummary())
    newissue.setProjectObject(projectObj)
    newissue.setIssueTypeId(taskIssueTypeID)
    newissue.reporter = currentUserObj
    newissue.assignee = currentUserObj
    newissue.description = issue.getDescription()
    newissue.priority = issue.getPriority()
    
} else if (issue.getIssueType().getName().equalsIgnoreCase("Improvement")) {
    if (requestTypeCFValue.equalsIgnoreCase("Suggest an Enhancement")) {
        Project projectObj = projectMgr.getProjectObjByKey("IB")
        currentUserObj = projectObj.getProjectLead()
        infoLog.info "CURRENT USER: ${currentUserObj.getName()}"
        newissue.setSummary ("Enhancement: " + issue.getSummary())
        newissue.setProjectObject(projectObj)
        newissue.setIssueTypeId(taskIssueTypeID)
        newissue.reporter = currentUserObj
        newissue.assignee = currentUserObj
        newissue.description = issue.getDescription()
        newissue.priority = issue.getPriority()

    } else if (requestTypeCFValue.equalsIgnoreCase("Request a New Project")) {
        Project projectObj = projectMgr.getProjectObjByKey("IB")
        currentUserObj = projectObj.getProjectLead()
        infoLog.info "CURRENT USER: ${currentUserObj.getName()}"
        newissue.setSummary ("New Project: " + issue.getSummary())
        newissue.setProjectObject(projectObj)
        newissue.setIssueTypeId(taskIssueTypeID)
        newissue.reporter = currentUserObj
        newissue.assignee = currentUserObj
        newissue.description = issue.getDescription()
        newissue.priority = issue.getPriority()
        
    }
} else if (issue.getIssueType().getName().equalsIgnoreCase("Report")) {
    Project projectObj = projectMgr.getProjectObjByKey("IB")
    currentUserObj = projectObj.getProjectLead()
    infoLog.info "CURRENT USER: ${currentUserObj.getName()}"
    newissue.setSummary ("Report: " + issue.getSummary())
    newissue.setProjectObject(projectObj)
    newissue.setIssueTypeId(taskIssueTypeID)
    newissue.reporter = currentUserObj
    newissue.assignee = currentUserObj
    newissue.description = issue.getDescription()
    newissue.priority = issue.getPriority()
    
} else if (issue.getIssueType().getName().equalsIgnoreCase("Data")) {
    if (requestTypeCFValue.equalsIgnoreCase("Data Update")) {
        Project projectObj = projectMgr.getProjectObjByKey("IB")
        currentUserObj = projectObj.getProjectLead()
        infoLog.info "CURRENT USER: ${currentUserObj.getName()}"
        newissue.setSummary ("Data Update: " + issue.getSummary())
        newissue.setProjectObject(projectObj)
        newissue.setIssueTypeId(taskIssueTypeID)
        newissue.reporter = currentUserObj
        newissue.assignee = currentUserObj
        newissue.description = issue.getDescription()
        newissue.priority = issue.getPriority()

    } else if (requestTypeCFValue.equalsIgnoreCase("Daily Merchandising Update")) {
        Project projectObj = projectMgr.getProjectObjByKey("EDU")
        currentUserObj = projectObj.getProjectLead()
        infoLog.info "CURRENT USER: ${currentUserObj.getName()}"
        newissue.setSummary (issue.getSummary())
        newissue.setProjectObject(projectObj)
        newissue.setIssueTypeId(taskIssueTypeID)
        newissue.reporter = currentUserObj
        newissue.assignee = currentUserObj
        newissue.description = issue.getDescription()
        newissue.priority = issue.getPriority()
    }
}*/

ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(currentUser)
Map params = new HashMap();
infoLog.info "NEWISSUE SUMMARY: ${newIssue.summary}"
params.put("issue", newIssue)
Issue linkedTask = issueMgr.createIssueObject(currentUser, params)
infoLog.info "Issue ${linkedTask.getKey()} created."

// get the current list of outwards depends on links to get the sequence number
IssueLinkManager linkMgr = ComponentAccessor.issueLinkManager
def sequence = 0
for (IssueLink link in linkMgr.getInwardLinks(sourceIssue.id)) {
    if (linkName == link.issueLinkType.name) {
        sequence++;
    }
}

linkMgr = ComponentAccessor.issueLinkManager
linkMgr.createIssueLink (newIssue.id, sourceIssue.id, Long.parseLong(linkNameID), Long.valueOf(sequence), currentUser)
infoLog.info "Issue ${linkedTask.getKey()} created and linked with request ${sourceIssue.key}."
ImportUtils.indexIssues = wasIndexing

MutableIssue createNewIssue (ProjectManager projectMgr, String projectKey, MutableIssue sourceIssue, MutableIssue destinationIssue, ApplicationUser currentUser, String issueTypeId, String summary) {
    MutableIssue newIssue = destinationIssue
    Project projectObj = projectMgr.getProjectObjByKey(projectKey)
    currentUser = projectObj.getProjectLead()
    infoLog.info "CURRENT USER: ${currentUser.getName()}"
    newIssue.setSummary (summary + sourceIssue.getSummary())
    newIssue.setProjectObject(projectObj)
    newIssue.setIssueTypeId(issueTypeId)
    newIssue.reporter = currentUser
    newIssue.assignee = currentUser
    newIssue.description = sourceIssue.getDescription()
    newIssue.priority = sourceIssue.getPriority()
    return newIssue
}