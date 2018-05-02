package fruitbagger.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.Optional;

public class Fruitbagger {

    private final String apikey;
    private final String base = "https://fruitbagger.herokuapp.com/api";
    private final JsonParser gson = new JsonParser();

    public Fruitbagger(String apiKey) {
        this.apikey = apiKey;
    }

    public void packFruitInBag(Integer sessionId, Integer bagId, Integer index) throws UnirestException {
        HttpResponse<String> pack = Unirest.post(base + "/bagging/" + sessionId + "/" + bagId + "/" + index)
                .header("auth", apikey)
                .asString();
        System.out.println("Current bag: " + pack.getBody());
    }

    public void closeSession(Integer sessionId) throws UnirestException {
        Unirest.put(base + "/session/" + sessionId)
                .header("auth", apikey)
                .asString();
    }

    public void closeBag(Integer sessionId, Integer bagId) throws UnirestException {
        Unirest.put(base + "/bag/" + sessionId + "/" + bagId)
                .header("auth", apikey)
                .asString();
    }

    public Optional<Fruit> retrieveFruit(Integer sessionId) throws UnirestException {
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

    public Integer getBag(Integer sessionId) throws UnirestException {
        HttpResponse<String> response = Unirest.post(base + "/bag/" + sessionId)
                .header("auth", apikey)
                .asString();
        return Integer.valueOf(response.getBody());
    }

    public Integer getSession() throws UnirestException {
        HttpResponse<String> response = Unirest.post(base + "/session")
                .header("auth", apikey)
                .asString();
        return Integer.valueOf(response.getBody());
    }
}
