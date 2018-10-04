package comp30022.server.twilio

const val TWILIO_APP_NAME: String = "GUGUGU"
const val TWILIO_FIREBASE_PUSH_CREDENTIAL: String = "CR32258191da074808a5975001f0bdc79b"
const val TWILIO_ACCOUNT_SID: String = "ACc564c5adcb834b833daff618d73d8a2e"
const val TWILIO_AUTH_TOKEN: String = "e5353cd242105dc49524a537330b5a85"
const val TWILIO_API_KEY: String = "SKaebd656fa23853424d9edf7ac792e8ef"
const val TWILIO_API_SECRET: String = "RFlJSI0ou4lboaiPyYo7RdL3qxZ6axem"
const val TWILIO_CHAT_SERVICE_SID: String = "ISf316b89375a04ec8ab22b3f7e6881625"
const val TWILIO_VOICE_APP_SID: String = "APc33df1e4ac6d49c2ae187ad3e62557eb"

data class RoomRecord(
    val sid: String, val type: String, val region: String, val maxParticipants: Int
)
