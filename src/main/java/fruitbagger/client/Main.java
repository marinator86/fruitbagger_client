package fruitbagger.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.Optional;

public class Main {

    private static final String apikey = "5aa2dace-0f1c-4388-9fd4-fca5d92e085f";
    private static final String base = "https://fruitbagger.herokuapp.com/api";
    private static final JsonParser gson = new JsonParser();

    public static void main(String[] args) throws Exception {
        System.out.println("Hi!");
        System.out.println("Api key: " + apikey);

        Integer sessionId = getSession();
        System.out.println("Creating session: " + sessionId);

        while (fillOneBag(sessionId) == Status.BAGFULL) ;

        closeSession(sessionId);
    }

    private static Status fillOneBag(Integer sessionId) throws UnirestException {
        Integer bagId = getBag(sessionId);
        Integer weight = 0;
        System.out.println("Opening bag: " + bagId);
        for (Optional<Fruit> nextFruit = retrieveFruit(sessionId); nextFruit.isPresent(); nextFruit = retrieveFruit(sessionId)) {
            packFruitInBag(sessionId, bagId, nextFruit.get().index);
            weight += nextFruit.get().weight;
            if (weight >= 1000) {
                closeBag(sessionId, bagId);
                return Status.BAGFULL;
            }
        }
        closeBag(sessionId, bagId);
        return Status.FINISHED;
    }

    private static void packFruitInBag(Integer sessionId, Integer bagId, Integer index) throws UnirestException {
        HttpResponse<String> pack = Unirest.post(base + "/bagging/" + sessionId + "/" + bagId + "/" + index)
                .header("auth", apikey)
                .asString();
        System.out.println("Current bag: " + pack.getBody());
    }

    private static void closeSession(Integer sessionId) throws UnirestException {
        Unirest.put(base + "/session/" + sessionId)
                .header("auth", apikey)
                .asString();
    }

    private static void closeBag(Integer sessionId, Integer bagId) throws UnirestException {
        Unirest.put(base + "/bag/" + sessionId + "/" + bagId)
                .header("auth", apikey)
                .asString();
    }

    private static Optional<Fruit> retrieveFruit(Integer sessionId) throws UnirestException {
        HttpResponse<String> fruit = Unirest.get(base + "/fruits/" + sessionId)
                .header("auth", apikey)
                .asString();
        if (fruit.getStatus() == 204)
            return Optional.empty();
        if (fruit.getStatus() == 400)
            return Optional.empty();

        JsonObject object = gson.parse(fruit.getBody()).getAsJsonObject();
        String index = object.keySet().toArray(new String[]{})[0];
        Integer weight = object.get(index).getAsInt();
        return Optional.of(new Fruit(Integer.valueOf(index), weight));
    }

    private static Integer getBag(Integer sessionId) throws UnirestException {
        HttpResponse<String> response = Unirest.post(base + "/bag/" + sessionId)
                .header("auth", apikey)
                .asString();
        return Integer.valueOf(response.getBody());
    }

    private static Integer getSession() throws UnirestException {
        HttpResponse<String> response = Unirest.post(base + "/session")
                .header("auth", apikey)
                .asString();
        return Integer.valueOf(response.getBody());
    }
}
