package de.mvpdt.mvp_dt.controller;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * https://openexchangerates.org/api/latest.json?app_id=da84d2fed1f34062af7e28694fc22e7f&base=USD&symbols=EUR
 * https://docs.openexchangerates.org/docs/latest-json
 * https://timestamp.online/
 */
@Controller
@RequestMapping(value = {"","/person"})
public class MainController {

    @RequestMapping(value = {"/","index"})
    public String index(Model model) throws URISyntaxException {
        String str = "https://openexchangerates.org/api/latest.json?app_id=da84d2fed1f34062af7e28694fc22e7f&base=USD&symbols=GBP%2CEUR";
        RestTemplate rs = new RestTemplate();
        JSONObject forObject = rs.getForObject(new URI(str), JSONObject.class);
        Integer timestamp = (Integer) forObject.get("timestamp");
        String base = (String)forObject.get("base");
        LinkedHashMap rates = (LinkedHashMap) forObject.get("rates");
//        System.out.println(personService.findAll());

        Date currentDate = new Date ((long)timestamp * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = dateFormat.format(currentDate);

        model.addAttribute("timestampAsInteger", timestamp);
        model.addAttribute("dataFromTimestamp", date);
        model.addAttribute("base_currency", base);
        model.addAttribute("rates", rates);

        return "index";
    }
}
