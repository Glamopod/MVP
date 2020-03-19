package de.mvpdt.mvp_dt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This controller is going to be called from the candleStickChart.jsp
 * and return a csv file with stock values.
 */
@Controller
@RequestMapping(value = {"","/"})
public class DataController {
    @RequestMapping(value = "/data")
    public String showChart() {
        return "USDJPY_M1_202003190000_202003192016.csv";
//        return "netflix-stock-price.csv";
    }
}

