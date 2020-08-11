package com.codecool.citystatistics.controller;

import com.codecool.citystatistics.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;


@RestController
public class Controller {

    @Autowired
    ApiCall apiCall;


    @GetMapping("/continent/{continent}")
    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
    public ArrayList<CitySmallCard> continentCities(@PathVariable String continent) throws IOException, JSONException {
        ArrayList<CitySmallCard> citySmallCards = new ArrayList<>();
        String searchContinentURL = "https://api.teleport.org/api/continents/geonames%3A"+continent+"/urban_areas/";
        JSONObject resultSlugs = apiCall.getResult(searchContinentURL);
        JSONArray slugs = resultSlugs.getJSONObject("_links").getJSONArray("ua:items");
        
        for (int i = 0; i < slugs.length(); i++){

            JSONObject citySlug = slugs.getJSONObject(i);
            String name =  citySlug.getString("name");
            String slug = citySlug.getString("href").substring(citySlug.getString("href").indexOf("slug:")+5, citySlug.getString("href").length()-1);
            
            JSONObject resultImage = apiCall.getResult(citySlug.getString("href").replaceAll("\\\\", "")+"images");
            String imageURL = resultImage.getJSONArray("photos").getJSONObject(0).getJSONObject("image").getString("web");
            
            CitySmallCard cityCard = CitySmallCard
                    .builder()
                    .cityName(name)
                    .citySlug(slug)
                    .cityImage(imageURL)
                    .build();

            citySmallCards.add(cityCard);

        }
        return citySmallCards;
    }



    @GetMapping("/cityalldata/{cityslug}")
    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
    public CityAllData result(@PathVariable String cityslug) throws IOException, JSONException {
        String URL = "https://api.teleport.org/api/urban_areas/slug:"+cityslug;
        ArrayList<Score> scoreArrayList = new ArrayList<>();
        ArrayList<Salary> salaryArrayList = new ArrayList<>();




        JSONObject resultName = apiCall.getResult(URL  + "/");
        String name = resultName.getString("name");


        JSONObject resultScores = apiCall.getResult(URL  + "/scores/");
        JSONArray categories = (JSONArray) resultScores.get("categories");

        for (int i = 0; i < categories.length(); i++) {
            String scoreName = categories.getJSONObject(i).getString("name");
            int scoreNumber = categories.getJSONObject(i).getInt("score_out_of_10");
            Score score = Score.builder()
                    .name(scoreName)
                    .score(scoreNumber)
                    .build();
            scoreArrayList.add(score);
        }


        JSONObject resultSalaries = apiCall.getResult(URL  + "/salaries/");
        JSONArray salaries = (JSONArray) resultSalaries.get("salaries");
        for (int i = 0; i < salaries.length(); i++) {
            
            String title =salaries.getJSONObject(i).getJSONObject("job").getString("title");
            int percentile_25 = salaries.getJSONObject(i).getJSONObject("salary_percentiles").getInt("percentile_25");
            int percentile_50 = salaries.getJSONObject(i).getJSONObject("salary_percentiles").getInt("percentile_50");
            int percentile_75 = salaries.getJSONObject(i).getJSONObject("salary_percentiles").getInt("percentile_75");
            Salary salary = Salary.builder()
                    .title(title)
                    .percentile_25(percentile_25)
                    .percentile_50(percentile_50)
                    .percentile_75(percentile_75)
                    .build();
            salaryArrayList.add(salary);
        }

        JSONObject resultImage = apiCall.getResult(URL + "/images/");
        String imageURL = resultImage.getJSONArray("photos").getJSONObject(0).getJSONObject("image").getString("web");


        return CityAllData
                .builder()
                .cityName(name)
                .citySlug(cityslug)
                .scores(scoreArrayList)
                .salaries(salaryArrayList)
                .image(imageURL)
                .build();
    }
}