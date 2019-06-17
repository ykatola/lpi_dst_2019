package lpi.client;

import java.io.File;
import java.io.IOException;

public interface MessageClient<T> {
    T ping();

    T echo(String message);

    T login(String receiver, String password);

    T message(String receiver, String message);

    T list();

    T sendFile(String receiver, File file) throws IOException;

    void exit();

    void listenTo();
}

