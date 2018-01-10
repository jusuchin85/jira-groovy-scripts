/**
 * @name calculate-due-date.groovy
 * @author justin.alex
 * @version 2.0
 * @date 24/02/2017
 *
 * Behaviours script to calculate due date based on Request Type
 */

import java.util.Calendar
import java.text.SimpleDateFormat
import java.sql.Timestamp
import com.onresolve.jira.groovy.user.FormField

// define custom fields
def requestTypeField = getFieldByName("Request Type")
def dueDateField = getFieldById("duedate")
def receivedDateField = getFieldById(getFieldChanged())

// define calendar entities for calculation
Calendar cal = Calendar.getInstance()
SimpleDateFormat sdf = new SimpleDateFormat("d/MMM/yy")

// initialise main entities for due date calculation
int dayOfWeek = 0
Date receivedDate = sdf.parse(receivedDateField.getFormValue().toString())
Date currentDate = cal.getTime()

if (receivedDate.after(currentDate)) {
    receivedDateField.setError("Received Date is after Created Date.")
} else {
    receivedDateField.clearError()
    // set due date to current date/time if request type is "Media"
    if (requestTypeField.getValue().toString().equals("Media")) {
        dueDateField.setFormValue(sdf.format(cal.getTime()))
    } else {
        if (requestTypeField.getValue() != null) {
            def days = numOfDays(requestTypeField.getValue().toString())
            if (days != 0) {
                cal.setTimeInMillis(receivedDate.getTime())
                while (dayOfWeek < days) {
                    cal.add(Calendar.DATE, 1)
                    dayOfWeek++
                    if (isWeekend(cal.get(Calendar.DAY_OF_WEEK)))
                        dayOfWeek--
                }

                dueDateField.setFormValue(sdf.format(cal.getTime()))
            } else {
                dueDateField.setFormValue("")
            }
        }
    }
}


// method to calculate due date
int numOfDays (String requestTypeValue) {
    if (requestTypeValue.equals("Internal Legal") || requestTypeValue.equals("Enquiry"))
        return 14
    else if (requestTypeValue.equals("GIPA"))
        return 5
    else if (requestTypeValue.equals("Budget Estimates Enquiry") || requestTypeValue.equals("HFN") || requestTypeValue.equals("Parliamentary Question"))
        return 7
    else if (requestTypeValue.equals("Internal Review") || requestTypeValue.equals("Internal Enquiry"))
        return 21
    else
        return 0
}

// method to check weekends
boolean isWeekend (int dayOfWeek) {
    dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY ? true : false
}
