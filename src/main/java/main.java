import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import server_pojo.Option;
import server_pojo.Root;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class main {

    public static final String englishVocabId = "3ea0b71b-8486-5914-a2db-a4994c02adb6";
    public static final String multiplicationTableId = "66f2a9aa-bac2-5919-997d-2d17825c1837";
    public static final String myUserId = "97cc1059-caf1-4d6f-ad58-66f52c8e16a7";
    public static final int loopAmount = 50;

    public static void main(String[] args) throws Exception {
//        runGameLoop();

        int numberOfThreads = 12;
        List<Thread> threadPool = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++){
            int finalI = i;
            Thread thread = new Thread(){
                @SneakyThrows
                @Override
                public void run() {
                    System.out.println("Thread " + finalI + " is running");
                    runGameLoop();
                }
            };
            threadPool.add(thread);
        }


        for (Thread thread : threadPool) {
            thread.start();
            Thread.sleep(100);
        }
        System.out.println(threadPool.size() + " threads are running");


    }

    public static void runGameLoop() throws Exception {
        int rice = 0;
        Root root = requestGame();
        for (int i = 0; i < loopAmount; i++) {
            root = answer(root);

            rice = root.getData().getAttributes().getRice();
            Thread.sleep(1300);
        }
        System.out.println("Total amount of rice gained: " + rice);
    }

    public static Root requestGame() throws IOException {
        URL url = new URL("https://engine.freerice.com/games?lang=en");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Accept", "application/json");
        http.setRequestProperty("Content-Type", "application/json");

//        String data = "{\n  \"category\": \"66f2a9aa-bac2-5919-997d-2d17825c1837\",\n  \"level\": 1,\n  \"user\": null \n}";
        String data = "{\n  \"category\": \"66f2a9aa-bac2-5919-997d-2d17825c1837\",\n  \"level\": 1,\n  \"user\": \"97cc1059-caf1-4d6f-ad58-66f52c8e16a7\" \n}";

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        InputStream inputStream = http.getInputStream();

        String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        ObjectMapper om = new ObjectMapper();
        Root root = om.readValue((response), Root.class);
        if (http.getResponseCode() != 200 || !http.getResponseMessage().equalsIgnoreCase("OK")) {
            System.out.println("Problem occured");
            System.out.println("Request game status: " + http.getResponseCode() + " " + http.getResponseMessage());
        }

        http.disconnect();
        return root;
    }

    public static Root answer(Root response) throws Exception {
        String urlString = buildURLForAnswering(response);
        URL url = new URL(urlString);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setDoOutput(true);
        http.setRequestProperty("Accept", "application/json");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        http.setRequestMethod("POST");

        ResponseAnswer answer = buildResponseAnswer(response, myUserId);
        //change user if user isnt null
//        String data = "{\n \"answer\": \"" + answer.answer + "\",\n \"question\": \"" + answer.question + "\",\n  \"user\": null \n}";

        String data = "{\n \"answer\": \"" + answer.answer + "\",\n \"question\": \"" + answer.question + "\",\n  \"user\": \"97cc1059-caf1-4d6f-ad58-66f52c8e16a7\" \n}";


        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        InputStream inputStream = http.getInputStream();
        String serverResponse = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper om = new ObjectMapper();
        Root root = om.readValue((serverResponse), Root.class);

        if (http.getResponseCode() != 200 || !http.getResponseMessage().equalsIgnoreCase("OK")) {
            System.out.println("Problem occured");
            System.out.println("Answer status: " + http.getResponseCode() + " " + http.getResponseMessage());
        }
        http.disconnect();
        return root;
    }


    private static ResponseAnswer buildResponseAnswer(Root response, String userId) throws Exception {
        return new ResponseAnswer(getAnswerToQuestion(response), getQuestionId(response), userId);
    }

    private static String buildURLForAnswering(Root response) {
        return "https://engine.freerice.com/games/" + getGameId(response) + "/answer?lang=en";
    }

    private static String getAnswerToQuestion(Root response) throws Exception {
        String question = getQuestion(response);
        List<Option> answerOptions = getAnswerOptions(response);

        String[] numbers = question.replace(" ", "").split("x");
        int answer = Integer.parseInt(numbers[0]) * Integer.parseInt(numbers[1]);
        for (Option answerOption : answerOptions) {
            if (Integer.parseInt(answerOption.text) == answer) {
                return answerOption.id;
            }
        }
        throw new Exception("No Answer found exception");
    }

    private static List<Option> getAnswerOptions(Root response) {
        ArrayList<Option> options = response.getData().getAttributes().getQuestion().getOptions();
        if (options.isEmpty()){
            System.out.println("Options are empty, printing response");
            System.out.println(response);
        }
        return options;
    }

    private static String getQuestion(Root response) {
        String text = response.getData().getAttributes().getQuestion().getText();
        if (text == null || text.isBlank()){
            System.out.println("game Id problem, printing response");
            System.out.println(response);
        }
        return text;
    }

    private static String getGameId(Root response) {
        String id = response.getData().getId();
        if (id == null || id.isBlank()){
            System.out.println("game Id problem, printing response");
            System.out.println(response);
        }
        return id;
    }

    private static String getQuestionId(Root response) {
        String id = response.getData().getAttributes().getQuestion_id();
        if (id == null || id.isBlank()){
            System.out.println("game Id problem, printing response");
            System.out.println(response);
        }
        return id;
    }
}
