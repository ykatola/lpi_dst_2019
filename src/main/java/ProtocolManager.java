final class ProtocolManager {

    enum Request {
        PING((byte) 1, "Ping", 0),
        ECHO((byte) 3, "Echo", 1),
        LOGIN((byte) 5, "Login", 2),
        LIST((byte) 10, "List", 0),
        MSG((byte) 15, "Msg", 2),
        FILE((byte) 20, "File", 3),
        RECEIVE_MESSAGE((byte) 25, "rcvmessage", 0),
        RECEIVE_FILE((byte) 30, "rcvfile", 0);

        final byte value;
        final String type;
        final int argumentsAmount;

        Request(byte value, String type, int argumentsAmount) {
            this.value = value;
            this.type = type;
            this.argumentsAmount = argumentsAmount;
        }
    }

    enum Response {
        PING_RESPONSE(2, "Ping is okay!"),
        SERVER_ERROR(100, "Server error happens!"),
        WRONG_SIZE(101, "Wrong size!"),
        SERIALIZATION(102, "Serialization error!"),
        UNKNOWN(103, "Unknown error!"),
        INCORRECT_PARAMETERS(104, "Incorrect parameters!"),
        WRONG_PASSWORD(110, "Wrong password!"),
        NOT_LOGGED_IN(112, "You need to login before do this!"),
        SENDING_FAILED(113, "Sending failed!"),
        LOGIN_OK_NEW(6, "Successful registration"),
        LOGIN_OK(7, "Successful login"),
        MSG_SENT(16, "Message send"),
        FILE_SENT(21, "File sent"),
        RECEIVE_MSG_EMPTY(26, "Empty message"),
        RECEIVE_FILE_EMPTY(31, "Empty file"),

        COMMON_OK_RESPONSE(50, "Cool"),
        NEED_TO_SERIALIZE_MESSAGE(51, "Serialization need"),
        COMMON_BAD_RESPONSE(400, "Something went wrong..");

        final int responseCode;
        final String information;

        Response(int responseCode, String information) {
            this.responseCode = responseCode;
            this.information = information;
        }
    }

}
