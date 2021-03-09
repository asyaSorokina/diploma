class User() {
    var state: State = State.BEGIN
    lateinit var currentTest: Test
    var lastMessageId: Int = 0
    fun generateId(): Int {
        lastMessageId++
        return lastMessageId
    }
}