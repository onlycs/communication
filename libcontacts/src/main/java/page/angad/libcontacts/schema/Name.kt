package page.angad.libcontacts.schema

import android.provider.ContactsContract.CommonDataKinds.StructuredName as DataName

object Name : DataKind<Name>(DataName.CONTENT_ITEM_TYPE) {
    val DisplayName = field<String?>(DataName.DISPLAY_NAME)
    val Prefix = field<String?>(DataName.PREFIX)
    val Given = field<String?>(DataName.GIVEN_NAME)
    val Middle = field<String?>(DataName.MIDDLE_NAME)
    val Family = field<String?>(DataName.FAMILY_NAME)
    val Suffix = field<String?>(DataName.SUFFIX)
    val PhoneticGiven = field<String?>(DataName.PHONETIC_GIVEN_NAME)
    val PhoneticMiddle = field<String?>(DataName.PHONETIC_MIDDLE_NAME)
    val PhoneticFamily = field<String?>(DataName.PHONETIC_FAMILY_NAME)
}
