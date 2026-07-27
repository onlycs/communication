package page.angad.libcontacts.schema

import android.provider.ContactsContract.CommonDataKinds.Organization as DataOrg

object Organization : DataKind<Organization>(DataOrg.CONTENT_ITEM_TYPE) {
    val Company = field<String?>(DataOrg.COMPANY)
    val Title = field<String?>(DataOrg.TITLE)
    val Department = field<String?>(DataOrg.DEPARTMENT)
}
