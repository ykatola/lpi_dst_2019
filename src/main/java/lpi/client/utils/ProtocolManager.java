package lpi.client.utils;

public final class ProtocolManager {

    public enum Request {
        PING((byte) 1, "ping", 0),
        ECHO((byte) 3, "echo", 1),
        LOGIN((byte) 5, "login", 2),
        LIST((byte) 10, "list", 0),
        MSG((byte) 15, "msg", 2),
        FILE((byte) 20, "file", 3),
        RECEIVE_MESSAGE((byte) 25, "rcvmessage", 0),
        RECEIVE_FILE((byte) 30, "rcvfile", 0),
        EXIT((byte) 100, "exit", 0);

        public final byte value;
        public final String type;
        public final int argumentsAmount;

        Request(byte value, String type, int argumentsAmount) {
            this.value = value;
            this.type = type;
            this.argumentsAmount = argumentsAmount;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public enum Response {
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

        public final int responseCode;
        public final String information;

        Response(int responseCode, String information) {
            this.responseCode = responseCode;
            this.information = information;
        }
    }

}
