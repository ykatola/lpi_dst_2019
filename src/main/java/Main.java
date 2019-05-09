import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Please, enter command with arguments: ");
        String input = sc.nextLine();
        String[] inputArray = input.split(" ");

        if (inputArray.length < 1) {
            throw new IllegalArgumentException("Input should contain at least one word!");
        }

        String command = inputArray[0];
        String[] parameters = new String[inputArray.length - 1];
        int count = 1;
        while (count < inputArray.length) {
            parameters[count - 1] = inputArray[count];
            count++;
        }
        System.out.println(String.format("Entered command = <%s>, parameters = %s", command, Arrays.toString(parameters)));
    }
}
