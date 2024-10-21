package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
    private static final String API_KEY = "ae25d50d-dc26-4b75-b690-efa3078befa6";
    private static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast?";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            String lat = getInput("Введите широту: ", scanner);
            String lon = getInput("Введите долготу: ", scanner);

            int limit = getValidInteger(scanner);

            String response = sendApiRequest(lat, lon, limit);

            if (response != null) {
                System.out.println("Ответ от Api: \n" + response);
                parseAndDisplayWeather(response, limit);
            }

        } catch (RuntimeException e) {
            System.err.println("Произошла ошибка: " + e.getMessage());
        }
    }

    private static String getInput(String text, Scanner scanner) {
        System.out.print(text);
        return scanner.nextLine();
    }

    private static int getValidInteger(Scanner scanner) {
        int value;
        while (true) {
            System.out.print("Введите количество дней для расчёта средней температуры: ");
            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                if (value > 0) {
                    break;
                }
                System.out.println("Пожалуйста, введите положительное число");
            } else {
                System.out.println("Пожалуйста, введите целое число");
                scanner.next(); 
            }
        }
        return value;
    }

    private static String sendApiRequest(String lat, String lon, int limit) {
        String urlString = BASE_URL + "lat=" + lat + "&lon=" + lon + "&limit=" + limit;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Yandex-API-Key", API_KEY);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder content = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    return content.toString();
                }
            } else {
                System.out.println("GET запрос не сработал. Код ответа: " + responseCode);
            }
        } catch (RuntimeException | IOException e) {
            System.err.println("Ошибка при отправке запроса: " + e.getMessage());
        }
        return null;
    }

    private static void parseAndDisplayWeather(String jsonResponse, int limit) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Получение текущей температуры
            if (jsonObject.has("fact")) {
                int currentTemp = jsonObject.getJSONObject("fact").getInt("temp");
                System.out.println("Текущая температура: " + currentTemp + "°C");
            }

            // Вычисление средней температуры за определенный период
            if (jsonObject.has("forecasts")) {
                JSONArray forecasts = jsonObject.getJSONArray("forecasts");
                double totalTemp = 0;
                int count = Math.min(limit, forecasts.length());

                for (int i = 0; i < count; i++) {
                    JSONObject day = forecasts.getJSONObject(i).getJSONObject("parts").getJSONObject("day");
                    int dayTemp = day.getInt("temp_avg");
                    totalTemp += dayTemp;
                    System.out.println("Температура за день " + (i + 1) + ": " + dayTemp + "°C");
                }

                double averageTemp = totalTemp / count;
                String result = String.format("%.2f", averageTemp);
                System.out.println("Средняя температура за " + count + " дней: " + result + " °C");
            }
        } catch (RuntimeException e) {
            System.err.println("Ошибка при обработке данных: " + e.getMessage());
        }
    }
}
