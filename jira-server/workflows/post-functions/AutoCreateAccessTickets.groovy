/**
  * Name: AutoCreateAccessTickets.groovy
  * @author jalex
  * @version 1.0
  *
  * A Groovy script to create access issues when a service request has been approved. The access issues are created
  * based on the selected systems and their respective owners. These owners are driven from a CSV file.
  *
  */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.IssueFactory
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.util.ImportUtils
import com.mindprod.csv.CSVReader
import java.util.HashMap

// Global variables
char separator = ','
char quote = '\"'
String comments = "#"

// Custom Field Names
String requestFor = "This Request Is For"
String firstName = "First Name"
String lastName = "Last Name"
String lineManager = "Line Manager"
String systemOwners = "System Owner(s)"
String addRemoveAccess = "Add / Remove System Access"
String systems = "System Accesses Required"
String systemAccessType = "System Access Type"
String systemAccessName = "System Access Name"
String serviceRequestType = "Service Request Type"
String singleBulkUser = "Single / Bulk User"

// Custom Fields
CustomFieldManager customFieldManager = ComponentAccessor.customFieldManager
CustomField requestForField = customFieldManager.getCustomFieldObjectByName(requestFor)
CustomField firstNameField = customFieldManager.getCustomFieldObjectByName(firstName)
CustomField lastNameField = customFieldManager.getCustomFieldObjectByName(lastName)
CustomField lineManagerField = customFieldManager.getCustomFieldObjectByName(lineManager)
CustomField systemOwnersField = customFieldManager.getCustomFieldObjectByName(systemOwners)
CustomField systemsField = customFieldManager.getCustomFieldObjectByName(systems)
CustomField addRemoveAccessField = customFieldManager.getCustomFieldObjectByName(addRemoveAccess)
CustomField systemAccessTypeField = customFieldManager.getCustomFieldObjectByName(systemAccessType)
CustomField systemAccessNameField = customFieldManager.getCustomFieldObjectByName(systemAccessName)
CustomField serviceRequestTypeField = customFieldManager.getCustomFieldObjectByName(serviceRequestType)
CustomField singleBulkUserField = customFieldManager.getCustomFieldObjectByName(singleBulkUser)

// Configurable section
def projectKey = "EN" // Project key of project you want to create the issue in
def issueTypeID = "11104"
def linkName = "Requests"
def linkNameID = "10500"
String userToLink = "AlexJ"
//def csvLocation = "D:/jira-data/scripts/system-owners.csv"
//def csvLocation = "C:/Program Files/Atlassian/Application Data/JIRA/scripts/system-owners.csv"
def csvLocation = "C:/Program Files/Atlassian/Application Data/JIRA/scripts/system-owners-2.csv"

IssueManager issueMgr = ComponentAccessor.issueManager
ProjectManager projectMgr = ComponentAccessor.projectManager
def userManager = ComponentAccessor.getUserUtil()
ApplicationUser userToLinkObj = issue.getReporter()

def serviceRequestTypeValue = issue.getCustomFieldValue(serviceRequestTypeField).toString()
def systemAccessTypeValue = issue.getCustomFieldValue(systemAccessTypeField).toString()
def singleBulkUserValue = issue.getCustomFieldValue(singleBulkUserField).toString()
def requestForValue = issue.getCustomFieldValue(requestForField).toString()
def fullName = ""

if (serviceRequestTypeValue.equalsIgnoreCase("Request for System Access (New Users, Change Users, Terminations)")) {
    
    // only execute the excerpt on a non-termination request
    if (!systemAccessTypeValue.equalsIgnoreCase("Termination of User Account")) {
    
        if (systemAccessTypeValue.equalsIgnoreCase("Onboarding New User(s)")) {
            if (!singleBulkUserValue.equalsIgnoreCase("Bulk User Request")) {
                // Full Name for User Requesting System Access
                def firstNameValue = issue.getCustomFieldValue(firstNameField).toString()
                def lastNameValue = issue.getCustomFieldValue(lastNameField).toString()
                fullName = firstNameValue + " " + lastNameValue
                
            }
        } else if (systemAccessTypeValue.equalsIgnoreCase("Modify System Access for Existing User")) {
            if (requestForValue.equalsIgnoreCase("Someone else")) {
                // Full Name for User Requesting System Access
                def firstNameValue = issue.getCustomFieldValue(firstNameField).toString()
                def lastNameValue = issue.getCustomFieldValue(lastNameField).toString()
                fullName = firstNameValue + " " + lastNameValue
            }
        }
    
        HashMap<String, String> hmap = new HashMap()

        // read system list and ownership from CSV file
        try {
            CSVReader reader = new CSVReader( new BufferedReader( new FileReader( csvLocation ) ),
                                separator,
                                quote,
                                true,
                                true)
            try {
                while ( true ) {
                    def system = reader.getAllFieldsInLine()
                    hmap.put(system[0], system[1])
                }
            } catch ( EOFException  e ) {
            }
                reader.close()
        } catch ( IOException  e ) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        
        def systemValues = issue.getCustomFieldValue(systemsField)
        
        for (String systemName : systemValues) {
            ArrayList<ApplicationUser> userList = new ArrayList<ApplicationUser>()
            
            // create new system access issue and link
            def wasIndexing = ImportUtils.indexIssues
            ImportUtils.indexIssues = true
            IssueFactory issueFactory = ComponentAccessor.issueFactory
            MutableIssue newIssue = issueFactory.getIssue()
            
            newIssue.setProjectObject(projectMgr.getProjectObjByKey(projectKey))
            newIssue.setIssueTypeId(issueTypeID)
            newIssue.description = ""
            newIssue.reporter = issue.getReporter()
            
            if (systemAccessTypeValue.equalsIgnoreCase("Onboarding New User(s)")) {
                if (!singleBulkUserValue.equalsIgnoreCase("Bulk User Request")) {
                    def newSummary = systemAccessTypeValue + ": " + systemName + " - " + fullName
                    newIssue.setSummary (newSummary)
                    newIssue.setCustomFieldValue (firstNameField, issue.getCustomFieldValue(firstNameField))
                    newIssue.setCustomFieldValue (lastNameField, issue.getCustomFieldValue(lastNameField))
                
                } else {
                    newIssue.setSummary (systemAccessTypeValue + ": " + systemName + " - Bulk User Request")
                }
                
            } else if (systemAccessTypeValue.equalsIgnoreCase("Modify System Access for Existing User")) {
                def addRemoveAccessValue = issue.getCustomFieldValue(addRemoveAccessField).toString()
            
                if (requestForValue.equalsIgnoreCase("Someone else")) {
                    def newSummary = addRemoveAccessValue + ": " + systemName + " - " + fullName
                    newIssue.setSummary (newSummary)
                    newIssue.setCustomFieldValue (firstNameField, issue.getCustomFieldValue(firstNameField))
                    newIssue.setCustomFieldValue (lastNameField, issue.getCustomFieldValue(lastNameField))

                } else {
                    def userDisplayName = issue.getReporter().getDisplayName()
                    def newSummary = addRemoveAccessValue + ": " + systemName + " - " + userDisplayName
                    newIssue.setSummary (newSummary)
                }
            }
            
            newIssue.setCustomFieldValue (lineManagerField, issue.getCustomFieldValue(lineManagerField))
            
            if (systemName == "JIRA") {
                def users = ["griffithsp"]
                users.each {
                    def user = userManager.getUserByName(it)
                    if(user) { 
                        userList.add(user)
                    }
                }
            } else if (systemName == "Confluence") {
                def users = ["valeriod"]
                users.each {
                    def user = userManager.getUserByName(it)
                    if(user) { 
                        userList.add(user)
                    }
                }
            } else if (systemName == "Test / Dev Server Access") {
                def users = ["mandekics"]
                users.each {
                    def user = userManager.getUserByName(it)
                    if(user) { 
                        userList.add(user)
                    }
                }
            } else if (systemName == "Misc - System Admin Access") {
                def users = ["haigj", "dot"]
                users.each {
                    def user = userManager.getUserByName(it)
                    if(user) { 
                        userList.add(user)
                    }
                }
            } else {
                String groupname = valueFromMap(hmap, systemName)
                def groupManager = ComponentAccessor.groupManager
                userList = groupManager.getUsersInGroup(groupname)
            }
            
            newIssue.setCustomFieldValue (systemOwnersField, userList)
            
            def optionToSet = ComponentAccessor.getOptionsManager().getOptions(systemAccessNameField.getRelevantConfig(issue)).find {option -> option.value == systemName}
            newIssue.setCustomFieldValue(systemAccessNameField, optionToSet)
            
            newIssue.setCustomFieldValue (addRemoveAccessField, issue.getCustomFieldValue(addRemoveAccessField))
            
            Map params = new HashMap();
            params.put("issue", newIssue)
            Issue linkedTask = issueMgr.createIssueObject(userToLinkObj, params)
            log.info "System Access Request ${linkedTask.getKey()} created."
              
            // get the current list of outwards depends on links to get the sequence number
            IssueLinkManager linkMgr = ComponentAccessor.issueLinkManager
            def sequence = 0
            for (IssueLink link in linkMgr.getInwardLinks(issue.id)) {
                if (linkName == link.issueLinkType.name) {
                    sequence++;
                }
            }
              
            linkMgr = ComponentAccessor.issueLinkManager
            linkMgr.createIssueLink (newIssue.id, issue.id, Long.parseLong(linkNameID), Long.valueOf(sequence), userToLinkObj)
            log.info "System Access Request ${linkedTask.getKey()} linked with service request ${issue.key}."
            ImportUtils.indexIssues = wasIndexing
        }
    }
}

// method to return group name from selected system
String valueFromMap(Map mp, String value) {
    Iterator it = mp.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        if (pair.getKey() == value)
        	return pair.getValue()
    }
    
    return null
}
