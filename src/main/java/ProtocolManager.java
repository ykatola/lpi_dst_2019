final class ProtocolManager {

    enum Request {
        PING((byte) 1, "Ping", 0),
        ECHO((byte) 3, "Echo", 1),
        LOGIN((byte) 5, "Login", 2),
        LIST((byte) 10, "List", 0),
        MSG((byte) 15, "Msg", 2),
        FILE((byte) 20, "File", 2),
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

}
