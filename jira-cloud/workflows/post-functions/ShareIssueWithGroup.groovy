/* @name    Justin Alex Paramanandan
 * @company ServiceRocket Pty Ltd
 * @date    02/03/2017
 * @version 1.4
 *
 * Script to validate and add the group to a single group picker custom field for issue visibility. The group to be added
 * is based on the group membership of the reporter.
 * 
 * CONDITION: issue.fields.reporter != null
 *
 */
  
// Define variables here
def groupToFind = 'Fleet Manager' // the group that we are attempting to share the issue with
def outputCfId = 'customfield_11000' // the file ID of the "Shared With" field
// comments that will be added, based on certain conditions as described
def moreThanOneGroupComment = 'Shared With field is not updated, as there are more than one group membership for the reporter.'
def noGroupsComment = 'There are no associated groups for the reporter. Please update the Shared With field manually.'
def commentVisibility = 'role' // the type of comment visibility setting to apply
def commentVisibilityValue = 'Administrators' // which role would be able to view the comment
 
// get the current reporter's name
String reporter = issue.fields.reporter.name

StringBuilder reporterBuilder = new StringBuilder();  
String[] splitReporter = reporter.split("\\s+")

int reporterIndex = 1
for (String reporterName : splitReporter) {
        reporterBuilder.append(reporterName)
        if (reporterIndex != splitReporter.length)
            reporterBuilder.append("+")
        reporterIndex++
      }
 
// REST API call to get the details of the reporter
def userResp = get("/rest/api/2/user/?username=${reporterBuilder.toString()}&expand=groups")
        .header('Content-Type', 'application/json')
        .asObject(Map)
assert userResp.status == 200
def user = userResp.body as Map
def userGroups = user.groups
def size = userGroups.size
 
// get the number of groups (based on the groupToFind vaiable) for the reporter
def numOfGroups = findNumberOfGroups(groupToFind, userGroups.items)

// if the number of groups (based on the groupToFind vaiable) found is just the only one, then we proceed
if (numOfGroups == 1) {
    // find the full group name
    String groupName = findSpecifiedGroup (groupToFind, userGroups.items)
    StringBuilder groupBuilder = new StringBuilder();  
    String[] splitGroup = groupName.split("\\s+")

    int groupIndex = 1
    for (String group : splitGroup) {
        groupBuilder.append(group)
        if (groupIndex != splitGroup.length)
            groupBuilder.append("+")
        groupIndex++
    }

    // REST API call to get the details of that group
    def groupResp = get("/rest/api/2/groups/picker?query=${groupBuilder.toString()}")
        .header('Content-Type', 'application/json')
        .asObject(Map)
    assert groupResp.status == 200
    def groupObj = groupResp.body as Map
 
    String foundGroupObjName = groupObj.groups[0].name
 
    // update the "Shared With" field with the found group name
    def result = put("/rest/api/2/issue/${issue.key}")
        .header('Content-Type', 'application/json')
        .body([
            update:[
                (outputCfId): [
                    [
                        set: [
                            name: foundGroupObjName
                        ]
                    ]
                ]
            ]
        ])
        .asString()
    if (result.status == 204) {
        return 'Issue ' + issue.key + ' is now shared with the ' + foundGroupObjName + ' group.'
    } else {
        return "${result.status}: ${result.body}"
    }
 
// if there are more than one group (based on the groupToFind vaiable) is found, we just add an internal comment to state so
} else if (numOfGroups > 1) {
    def result = post("/rest/api/2/issue/${issue.key}/comment")
        .header('Content-Type', 'application/json')
        .body([
            body: moreThanOneGroupComment,
            visibility: [
                type: commentVisibility,
                value: commentVisibilityValue
            ]
        ])
        .asString()
    if (result.status == 201) {
        return 'MORE THAN ONE GROUP COMMENT ADDED TO ' + issue.key
    } else {
        return "${result.status}: ${result.body}"
    }
 
// if there are no groups (based on the groupToFind vaiable) found, add an internal comment to state so
} else {
    def result = post("/rest/api/2/issue/${issue.key}/comment")
        .header('Content-Type', 'application/json')
        .body([
            body: noGroupsComment,
            visibility: [
                type: commentVisibility,
                value: commentVisibilityValue
            ]
        ])
        .asString()
    if (result.status == 201) {
        return 'NO GROUPS COMMENT ADDED TO ' + issue.key
    } else {
        return "${result.status}: ${result.body}"
    }
}
 
// method to find the number of groups for a user, based on a specific group prefix
int findNumberOfGroups (String groupPrefix, ArrayList userGroups) {
    def matchingString = 0
 
    for (def groupObj : userGroups) {
        String groupName = groupObj.name
 
        // once we found the grouop we want, we increase the size of the matching string variable
        if (groupName.startsWith(groupPrefix))
            matchingString++
    }
 
    return matchingString
}
 
// method to find the group name for a user, based on a specific group prefix
// note: only executed if there is one group foun
String findSpecifiedGroup (String groupPrefix, ArrayList userGroups) {
     
    for (def groupObj : userGroups) {
        String groupName = groupObj.name
 
        // once we found the grouop we want, return the group's name
        if (groupName.startsWith(groupPrefix))
            return groupName
    }
 
    return null
}