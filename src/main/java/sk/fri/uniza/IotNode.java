package sk.fri.uniza;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import sk.fri.uniza.api.WeatherStationService;
import sk.fri.uniza.model.Token;
import sk.fri.uniza.model.WeatherData;

import java.io.IOException;
import java.util.List;

public class IotNode {
    private final Retrofit retrofit;
    private final WeatherStationService weatherStationService;

    public IotNode() {
        retrofit = new Retrofit.Builder()
                // Url adresa kde je umietnená WeatherStation služba
                .baseUrl("http://ip172-18-0-19-bqvi2hiosm4g00b8ekd0-9000.direct.labs.play-with-docker.com/")
                // Na konvertovanie JSON objektu na java POJO použijeme
                // Jackson knižnicu
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        // Vytvorenie inštancie komunikačného rozhrania
        weatherStationService = retrofit.create(WeatherStationService.class);
    }

    public WeatherStationService getWeatherStationService() {
        return weatherStationService;
    }

    public String getToken() {
        Call<Token> tokenCall = getWeatherStationService().getToken("Basic YWRtaW46aGVzbG8=", List.of("all"));
        String token = null;

        try {
            Response<Token> response = tokenCall.execute();

            if(response.isSuccessful()) {
                Token body = response.body();

                assert body != null;
                token = body.getToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return token;
    }

    public double getAverageTemperature(String authorization, String station, String from, String to) {
        double averageTemp = 0;
        Call<List<WeatherData>> historyWeather = weatherStationService.getHistoryWeatherAuth(authorization, station, from, to, List.of("airTemperature"));

        try {
            Response<List<WeatherData>> response = historyWeather.execute();

            if(response.isSuccessful()) {
                List<WeatherData> weatherData = response.body();

                assert weatherData != null;
                for(WeatherData data: weatherData) averageTemp += data.getAirTemperature();

                averageTemp /= weatherData.size();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return averageTemp;
    }
}
