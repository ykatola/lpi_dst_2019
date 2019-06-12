package lpi.client;

public interface MessageClient<T> {
    T ping();

    T echo(String message);

    T login(String receiver, String password);

    T message(String receiver,String message);

    T list();

    default String unknownErrorMessage() {
        return "Unknown error happened...";
    }
}
