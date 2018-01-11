/**
  * Name: ValidateTwoDatesWorkflow.groovy
  * @author jalex
  * @version 2.1
  *
  * A Groovy script to compare the Due Date field against the Resolved Date field. If the Resolved Date is after the
  * Due Date, then the Target Met field would be set to "No". Otherwise, it would be set to "Yes"
  *
  */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import java.text.SimpleDateFormat

// Constants declarations
def targetMetFieldName = "Target Met"
Long targetMetYesOptionID = 10001
Long targetMetNoOptionID = 10002

// We grab the related fields here
CustomFieldManager customFieldManager = ComponentAccessor.CustomFieldManager
CustomField targetMetField = customFieldManager.getCustomFieldObjectByName(targetMetFieldName)
String dueDateValue = issue.getDueDate()
String resolvedDateValue = issue.getResolutionDate()

// Some error checking
if (dueDateValue.equals(null)) {
    log.info "No due date specified for issue ${issue.getKey()}"
} else if (resolvedDateValue.equals(null)) {
    log.info "No resolved date specified for issue ${issue.getKey()}"
} else if (targetMetField.equals(null)) {
    log.info "Custom field ${targetMetFieldName} does not exist in the instance."
}

// Parsing the Resolved Date and Due Date fields as a Date() object
Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(dueDateValue)
Date resolveDate = new SimpleDateFormat("yyyy-MM-dd").parse(resolvedDateValue)

log.info "Due Date for ${issue.getKey()} is ${dueDate.toString()}"
log.info "Resolved Date for ${issue.getKey()} is ${resolveDate.toString()}"

// We grab the available options from the custom field
Object selectValue = issue.getCustomFieldValue(targetMetField) //grab current value for the custom field
FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem(targetMetField)
OptionsManager optionsManager = (OptionsManager) ComponentAccessor.getComponentOfType(OptionsManager.class)
Option targetMetNoOption = optionsManager.findByOptionId(targetMetNoOptionID)
Option targetMetYesOption = optionsManager.findByOptionId(targetMetYesOptionID)

if (targetMetNoOption.equals(null)) {
    log.info "There is no option with the ID ${targetMetNoOptionID}"
} else if (targetMetYesOption.equals(null)) {
    log.info "There is no option with the ID ${targetMetYesOptionID}"
}

// Date comparisons go here
if (resolveDate.after(dueDate)) {
    targetMetField.updateValue(fieldLayoutItem, issue, new ModifiedValue(selectValue, targetMetNoOption), new DefaultIssueChangeHolder())
    log.info "Custom field ${targetMetField.getFieldName()} for issue ${issue.getKey()} is set to No"

} else {
    targetMetField.updateValue(fieldLayoutItem, issue, new ModifiedValue(selectValue, targetMetYesOption), new DefaultIssueChangeHolder())
    log.info "Custom field ${targetMetField.getFieldName()} for issue ${issue.getKey()} is set to Yes"
}