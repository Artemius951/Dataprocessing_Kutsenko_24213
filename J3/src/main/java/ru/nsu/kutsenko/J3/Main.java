package ru.nsu.kutsenko.J3;

public class Main {

    public static void main(String[] args)
        throws InterruptedException {

        if (args.length > 1) {
            System.err.println(
                "Usage: gradlew run --args=\"[port]\""
            );
            System.exit(1);
        }

        int port = 8080;

        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println(
                    "The port must be an integer."
                );
                System.exit(1);
            }
        }

        Spider spider = new Spider("localhost", port);

        for (String message : spider.crawl()) {
            System.out.println(message);
        }
    }
}