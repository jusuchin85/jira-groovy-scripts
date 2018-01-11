import com.atlassian.jira.bc.user.search.UserPickerSearchService
import com.atlassian.jira.bc.user.search.UserSearchParams
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager

CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
def cf = customFieldManager.getCustomFieldObjectByName("Approver")
if (cf == null) {
    log.error("No custom field found");
    return;
}
def user = issue.getCustomFieldValue(cf);

def userSearchService = ComponentAccessor.getComponent(UserPickerSearchService)
def approverUser = userSearchService.findUsers(user.toString(), UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY)

if (approverUser) {
    approverUser.first() // An ApplicationUser in JIRA 6
    log.debug("Current defined approver is " + approverUser.first().getDisplayName())
    issue.setAssignee(approverUser.first())
    log.debug "Issue ${issue.key} assigned to " + approverUser.first().getDisplayName() + " for approval."
    issue.store()
}
else {
    // no users found with that display name
    log.debug("No approver with the name " + user + "found.")
}