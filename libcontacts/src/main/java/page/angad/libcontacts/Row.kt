package page.angad.libcontacts

interface Row {
    val mime: String
}

interface Property<T> {
    val parent: Row
}