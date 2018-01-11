/**
  * @author justin.alex
  * @version 1.0
  *
  * A Groovy script to copy the parent and child of the Category cascading select field into
  * the parent category and child category select lists. These fields can then be used
  * for reporting.
  *
  */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.fields.config.FieldConfigScheme
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder

// USER DEFINED VARIABLES
def categoryFieldName = "Category"
def categoryFieldID = "customfield_11706"
def parentCategoryFieldID = "customfield_12303"
def childCategoryFieldID = "customfield_12304"

// INIT FIELD OBJECTS
def customFieldManager = ComponentAccessor.customFieldManager
def userCategoryField = customFieldManager.getCustomFieldObject(categoryFieldID)
def parentCategoryField = customFieldManager.getCustomFieldObject(parentCategoryFieldID)
def childCategoryField = customFieldManager.getCustomFieldObject(childCategoryFieldID)

MutableIssue issue = (MutableIssue) event.issue
Map<String, Option> params = (HashMap<String,Option>) issue.getCustomFieldValue(userCategoryField)

if (params != null) {
    Option parent = params.get(CascadingSelectCFType.PARENT_KEY)
    Option child = params.get(CascadingSelectCFType.CHILD_KEY)
    
    // add options to the select list
    Option parentOption = addOptionToCustomField(parentCategoryField, parent.getValue())
    Option childOption = addOptionToCustomField(childCategoryField, child.getValue())

    // Update parent and child select lists
    if (parentOption != null && childOption != null) {
        parentCategoryField.updateValue(null, issue , new ModifiedValue(issue.getCustomFieldValue(parentCategoryField), parentOption), new DefaultIssueChangeHolder());
        childCategoryField.updateValue(null, issue , new ModifiedValue(issue.getCustomFieldValue(childCategoryField), childOption), new DefaultIssueChangeHolder());
    }
}

// Method to add new option to select list
public Option addOptionToCustomField(CustomField customField, String value) {
    Option newOption = null
    if (customField != null) {
        List<FieldConfigScheme> schemes = customField.getConfigurationSchemes()
        if (schemes != null && !schemes.isEmpty()) {
            FieldConfigScheme sc = schemes.get(0)
            Map configs = sc.getConfigsByConfig()
            if (configs != null && !configs.isEmpty()) {
                FieldConfig config = (FieldConfig) configs.keySet().iterator().next()
                OptionsManager optionsManager = ComponentAccessor.optionsManager
                Options options = optionsManager.getOptions(config)

                // check if passed option already exists
                if (!newValueAlreadyPresent(options, value)) {

                    // if passed option doesn't exist, add them to the select list
                    int nextSequence = options.isEmpty() ? 1 : options.getRootOptions().size() + 1
                    newOption = optionsManager.createOption(config, null, (long) nextSequence, value)
                    log.warn "Option ${value} added to ${customField.getFieldName()}."
                } else {

                    // passed option exists, get existing option
                    newOption = getCurrentOption(options, value)
                    log.warn "Option ${value} already exist."
                }
            }
        }
    }

    return newOption;
}

// method to check existence of value in select list
private boolean newValueAlreadyPresent(List<Option> rootOptions, String value) {
    for (Option o : rootOptions) {
        if (o.getValue().equalsIgnoreCase(value))
            return true
    } 
    return false
}

// method to return current option from select list
private Option getCurrentOption (List<Option> rootOptions, String value) {
    for (Option o : rootOptions) {
        if (o.getValue().equals(value))
            return o;
    }
}
