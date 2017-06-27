package com.netease.onmyouji.taskjob;

import com.netease.onmyouji.entity.Shikigami;
import com.netease.onmyouji.service.ShikigamiService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class SyncShikigamiData {

    @Autowired
    private ShikigamiService shikigamiService;

    // 每天00点执行任务调度
    @Scheduled(cron = "10 * * * * ?")
    public void syncShikigamiDataTaskJob() {
        HashMap<String, Integer> rarityMap = new HashMap<String, Integer>() {{
            put("SSR", 1);
            put("SR", 2);
            put("R", 3);
            put("N", 4);
        }};
        String[] pro = {"attack", "life", "defense", "speed", "crit"};
        String targetUrl = "http://news.4399.com/yyssy/shishenlu/";
        Document targetDoc;
        try {
            targetDoc = Jsoup.connect(targetUrl).get();
            Elements targetElem = targetDoc.select(".clist.l120.cf").eq(0).select("li");
            for (int i = 0; i < targetElem.size(); i++) {
                Element currElem = targetElem.get(i);
                String shikigamiName = currElem.children().text();
                String shikigamiDetailUrl = currElem.children().attr("href");
                // 获取式神的稀有度
                int rarity = rarityMap.get(shikigamiDetailUrl.substring(
                        shikigamiDetailUrl.indexOf("shishenlu/") + "shishenlu/".length(),
                        shikigamiDetailUrl.indexOf("/m")).toUpperCase());
                Document shikigamiDetailDoc = Jsoup.connect(shikigamiDetailUrl).get();
                Elements shikigamiDetail = shikigamiDetailDoc.select(".roledata li");
                List<HashMap<String, String>> shikigamiDetailList = new ArrayList<>();
                HashMap<String, String> shikigamiDetailMap = new HashMap<>();
                // 抓取式神的面板属性
                for (int j = 5; j < shikigamiDetail.size(); j++)
                    shikigamiDetailMap.put(pro[j - 5], shikigamiDetail.get(j).text());
                shikigamiDetailList.add(shikigamiDetailMap);
                System.out.println(shikigamiName + ": " + shikigamiDetailMap);
                List<Shikigami> shikigamis = shikigamiService.getAllShikigamis();

                for (Shikigami shikigamiItem: shikigamis) {
                    if (!shikigamiItem.getName().equals(shikigamiItem)) {
                        Shikigami shikigami = new Shikigami();
                        shikigami.setName(shikigamiName);
                        shikigami.setRarity(rarity);
                        shikigamiService.addShikigami(shikigami);
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}