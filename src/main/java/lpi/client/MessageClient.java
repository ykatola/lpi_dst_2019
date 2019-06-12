package lpi.client;

public interface MessageClient<T> {
    T ping();

    T echo(String message);

    T login(String receiver, String password);

    T message(String receiver,String message);

    T list();

    void exit();
}
