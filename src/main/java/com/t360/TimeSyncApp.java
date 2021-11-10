package com.t360;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class TimeSyncApp {

    public static void main(String[] args) throws IOException, URISyntaxException {
        try (InputStream is = args.getClass().getClassLoader().getResourceAsStream("page.html")) {

            byte[] buffer = new byte[1024];
            StringBuilder htmlPage = new StringBuilder();
            while (is.available() > 0) {
                int read = is.read(buffer);
                htmlPage.append(new String(buffer, 0, read));
            }

            System.out.println(htmlPage);

        }
    }
}