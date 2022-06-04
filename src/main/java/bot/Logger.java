package bot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private BufferedWriter writer;

    public Logger(){
        try {
            writer = new BufferedWriter(new FileWriter(".\\log.txt", true));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void log (Long chatId, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------------\n")
                .append("---")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyy | HH:mm:ss.nnn")))
                .append("---\n")
                .append("chat Id: ")
                .append(chatId)
                .append("\n")
                .append("message: ")
                .append(message)
                .append("\n");

        System.out.println(sb.toString());

        try {
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
