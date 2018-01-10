/**
 * @name validate-subtask-due-date.groovy
 * @author justin.alex
 * @date 24/02/2017
 * @version 2.0
 *
 * Behaviours script to validate updated sub-task due date against the parent's due date.
**/
 
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.jira.groovy.user.FieldBehaviours
import com.onresolve.jira.groovy.user.FormField
import java.text.SimpleDateFormat
import java.sql.Timestamp
import groovy.transform.BaseScript
 
@BaseScript FieldBehaviours fieldBehaviours
 
FormField dueDateField = getFieldById(getFieldChanged())
FormField parent = getFieldById("parentIssueId")
Long parentIssueId = parent.getFormValue() as Long
 
def issueManager = ComponentAccessor.getIssueManager()
def parentIssue = issueManager.getIssueObject(parentIssueId)
 
SimpleDateFormat sdf = new SimpleDateFormat("d/MMM/yy")
 
if (dueDateField.getFormValue() != null && !dueDateField.getFormValue().toString().equals("")) {
    Date parentDueDate = new Date (parentIssue.getDueDate().getTime())
 
    Date subTaskDueDate = sdf.parse(dueDateField.getFormValue().toString())
     
    if (subTaskDueDate.after(parentDueDate))
        dueDateField.setError("Due date must be before parent issue's due date.")
    else
        dueDateField.clearError()
}
 