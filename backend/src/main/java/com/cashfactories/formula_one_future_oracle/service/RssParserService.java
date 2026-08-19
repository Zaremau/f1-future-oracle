package com.cashfactories.formula_one_future_oracle.service;

import com.cashfactories.formula_one_future_oracle.model.Driver;
import com.cashfactories.formula_one_future_oracle.model.GrandPrix;
import com.cashfactories.formula_one_future_oracle.model.News;
import com.cashfactories.formula_one_future_oracle.repository.DriverRepository;
import com.cashfactories.formula_one_future_oracle.repository.GrandPrixRepository;
import com.cashfactories.formula_one_future_oracle.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для парсинга RSS-лент новостей Формулы 1.
 * Скачивает новости, фильтрует их по релевантности Гран-при,
 * анализирует тональность через Python-скрипт и сохраняет в базу данных.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RssParserService {

    private final NewsRepository newsRepo;
    private final GrandPrixRepository gpRepo;
    private final DriverRepository driverRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String[] RSS_FEEDS = {
            "https://www.motorsport.com/rss/f1/news/",
            "https://www.formel1.de/rss.xml"
    };

    private static final List<String> RISK_KEYWORDS = Arrays.asList(
            "grid penalty", "engine penalty", "gearbox", "crash", "rain", "weather", "damage", "dnf"
    );

    private static final String PYTHON_SCRIPT_PATH = "/app/python-sentiment/sentiment.py";

    /**
     * Главный метод: скачивает новости и запускает их обработку.
     *
     * @param gpId идентификатор Гран-при, для которого скачиваются новости
     */
    public void fetchAndProcessNews(Long gpId) {
        GrandPrix gp = gpRepo.findById(gpId)
                .orElseThrow(() -> new NoSuchElementException("Гран-при с ID " + gpId + " не найден"));

        for (String url : RSS_FEEDS) {
            try {
                String xmlData = restTemplate.getForObject(url, String.class);
                parseAndSaveXml(xmlData, gp);
            } catch (Exception e) {
                log.error("Ошибка при получении RSS ленты {}: {}", url, e.getMessage());
            }
        }

        processNews();
    }

    /**
     * Парсит XML и сохраняет сырые новости в базу данных.
     *
     * @param xmlData строка XML, полученная из RSS-ленты
     * @param gp объект Гран-при, к которому привязываются новости
     */
    private void parseAndSaveXml(String xmlData, GrandPrix gp) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes()));

            NodeList items = doc.getElementsByTagName("item");

            for (int i = 0; i < items.getLength(); i++) {
                Node node = items.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String title = element.getElementsByTagName("title").item(0).getTextContent();
                    String link = element.getElementsByTagName("link").item(0).getTextContent();

                    if (!isNewsRelevant(title, gp) || newsRepo.existsByGrandPrix_IdAndUrl(gp.getId(), link)) {
                        continue;
                    }

                    News news = new News();
                    news.setGrandPrix(gp);
                    news.setTitle(title);
                    news.setUrl(link);
                    news.setSource(extractSourceFromUrl(link));
                    news.setPublishedAt(LocalDateTime.now());
                    news.setIsProcessed(false);
                    news.setRawXml(element.toString());

                    newsRepo.save(news);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка парсинга XML: {}", e.getMessage());
        }
    }

    /**
     * Проверяет, связана ли новость с конкретным Гран-при (по названию или стране).
     *
     * @param title заголовок новости
     * @param gp объект Гран-при
     * @return true, если новость релевантна
     */
    private boolean isNewsRelevant(String title, GrandPrix gp) {
        if (title == null || title.isEmpty()) return false;

        String lowerTitle = title.toLowerCase();
        String gpName = gp.getName().toLowerCase();
        String country = gp.getCountry().toLowerCase();
        String mainKeyword = gpName.replace("grand prix", "").trim();

        return lowerTitle.contains(mainKeyword) || lowerTitle.contains(country);
    }

    /**
     * Анализ сырых новостей: вызов Python для анализа тональности,
     * поиск ключевых слов риска и имен пилотов.
     */
    public void processNews() {
        List<News> unprocessed = newsRepo.findByIsProcessedFalse();
        List<Driver> allDrivers = driverRepo.findAll();

        for (News news : unprocessed) {
            double sentiment = callPythonSentiment(news.getTitle());
            news.setSentimentScore(sentiment);

            String[] keywords = checkRiskKeywords(news.getTitle());
            news.setRiskKeywords(keywords);

            String[] mentionedDrivers = findMentionedDrivers(news.getTitle(), allDrivers);
            news.setMentionedDrivers(mentionedDrivers);

            news.setIsProcessed(true);
            newsRepo.save(news);
        }
    }

    /**
     * Извлекает имя источника из URL.
     *
     * @param url ссылка на новость
     * @return имя источника (например, motorsport.com)
     */
    private String extractSourceFromUrl(String url) {
        if (url.contains("motorsport.com")) return "motorsport.com";
        if (url.contains("formel1.de")) return "formel1.de";
        return "unknown";
    }

    /**
     * Ищет имена пилотов в тексте новости.
     *
     * @param text текст новости
     * @param drivers список всех пилотов
     * @return массив имен упомянутых пилотов
     */
    private String[] findMentionedDrivers(String text, List<Driver> drivers) {
        String lowerText = text.toLowerCase();
        return drivers.stream()
                .filter(d -> lowerText.contains(d.getName().toLowerCase()))
                .map(Driver::getName)
                .toArray(String[]::new);
    }

    /**
     * Определяет тональность новости через вызов Python-скрипта (TextBlob).
     *
     * @param text текст новости
     * @return оценка тональности от -1.0 до 1.0
     */
    private double callPythonSentiment(String text) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", PYTHON_SCRIPT_PATH, text);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String result = reader.readLine();

                boolean finished = process.waitFor(5, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    return 0.0;
                }

                if (result != null && !result.isEmpty()) {
                    return Double.parseDouble(result.trim());
                }
            }
        } catch (Exception e) {
            log.error("Ошибка вызова Python скрипта: {}", e.getMessage());
        }
        return 0.0;
    }

    /**
     * Проверяет текст на наличие ключевых слов риска (штрафы, аварии, погода).
     *
     * @param text текст новости
     * @return массив найденных ключевых слов
     */
    private String[] checkRiskKeywords(String text) {
        if (text == null || text.isEmpty()) return new String[0];

        String lowerText = text.toLowerCase();

        return RISK_KEYWORDS.stream()
                .filter(lowerText::contains)
                .toArray(String[]::new);
    }
}