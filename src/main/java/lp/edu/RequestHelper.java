package lp.edu;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequestHelper {

    static String[] splitCommand(String source) {
        List<String> params = Arrays.asList(Arrays.copyOfRange(source.split(" "), 1, source.length()));
        params = params.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return params.toArray(new String[0]);
    }

    static Optional<ProtocolManager.Request> mapToRequest(String that) {
        if (that == null || that.isEmpty()) {
            throw new IllegalArgumentException("Command should be not null and not empty!");
        }
        String[] arguments = that.split(" ");
        String command = arguments[0];
        List<ProtocolManager.Request> requestCommands = Arrays.asList(ProtocolManager.Request.values());
        return requestCommands.stream()
                .filter(request -> request.type.equalsIgnoreCase(command))
                .findFirst();
    }
}
