package de.mvpdt.mvp_dt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"","/"})
public class CandleStickChartController {
    @RequestMapping("/chart")
    public String showChart() {
        return "candleStickChart";
    }
}
