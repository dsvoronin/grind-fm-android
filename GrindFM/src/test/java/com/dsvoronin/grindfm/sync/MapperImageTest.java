package com.dsvoronin.grindfm.sync;

import com.dsvoronin.grindfm.BuildConfig;
import com.dsvoronin.grindfm.entities.Article;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MapperImageTest {

    String testHtml = "<p> Четвертый выпуск нашего шоу \"О стримерах с любовью\" пройдет сегодня в 21:00. В гости к Grind.FM внезапно заглянет популярный стример Денис \"WELOVEGAMES\"!\n" +
            "</p>\n" +
            "<p><center><a href=\"http://www.grind.fm/radio\" target=\"_blank\"><img src=\"http://www.grind.fm/files/grind.fm/wlgaswl_0.png\" class=\"news\" width=\"800px\" alt=\"\" /></a></center></p>\n" +
            "<p>Он уже посещал нашу виртуальную студию в октябре прошлого года, с записью прошедшего эфира можете ознакомиться по этой ссылке <a href=\"http://www.grind.fm/node/1566\" title=\"http://www.grind.fm/node/1566\">http://www.grind.fm/node/1566</a> (мы уверены, что вы получите ответы на многие свои вопросы, прослушав запись). Сегодня мы продолжим задавать вопросы, которые вы давно хотели ему задать, но никак не получалось. Также мы решили провести небольшой эксперимент, поэтому сегодняшний эфир будет проходить не только на нашей радиостанции, но и параллельно на официальном канале трансляций Дениса: <a href=\"http://twitch.tv/welovegames\" title=\"http://twitch.tv/welovegames\">http://twitch.tv/welovegames</a>.</p>\n" +
            "<p><b>Свои вопросы вы можете задавать в чате:</b> <a href=\"http://www.grind.fm/radio\" title=\"http://www.grind.fm/radio\">http://www.grind.fm/radio</a></p>\n" +
            "<p><b>Основной канал трансляций:</b> <a href=\"http://twitch.tv/welovegames\" title=\"http://twitch.tv/welovegames\">http://twitch.tv/welovegames</a><br />\n" +
            "<b>Twitter:</b> WELOVEGAMESTV<br />\n" +
            "<b>Vkontakte:</b> <a href=\"http://vk.com/welovegames\" title=\"http://vk.com/welovegames\">http://vk.com/welovegames</a></p>\n" +
            "<!--break--><!--break--><script type=\"text/javascript\" src=\"//yastatic.net/share/share.js\" charset=\"utf-8\"></script><div style=\"float: right;\" class=\"yashare-auto-init\" data-yashareL10n=\"ru\" data-yashareType=\"icon\" data-yashareQuickServices=\"vkontakte,facebook,twitter\"></div>\n" +
            "<p><meta property=\"og:image\" content=\"http://www.grind.fm/files/grind.fm/wlgaswl_0.png\" /></p>\n";

    String expected = "Четвертый выпуск нашего шоу \"О стримерах с любовью\" пройдет сегодня в 21:00. В гости к Grind.FM внезапно заглянет популярный стример Денис \"WELOVEGAMES\"! \n" +
            "\n" +
            "￼ \n" +
            "\n" +
            "Он уже посещал нашу виртуальную студию в октябре прошлого года, с записью прошедшего эфира можете ознакомиться по этой ссылке http://www.grind.fm/node/1566 (мы уверены, что вы получите ответы на многие свои вопросы, прослушав запись). Сегодня мы продолжим задавать вопросы, которые вы давно хотели ему задать, но никак не получалось. Также мы решили провести небольшой эксперимент, поэтому сегодняшний эфир будет проходить не только на нашей радиостанции, но и параллельно на официальном канале трансляций Дениса: http://twitch.tv/welovegames.\n" +
            "\n" +
            "Свои вопросы вы можете задавать в чате: http://www.grind.fm/radio\n" +
            "\n" +
            "Основной канал трансляций: http://twitch.tv/welovegames\n" +
            "Twitter: WELOVEGAMESTV\n" +
            "Vkontakte: http://vk.com/welovegames\n\n";

    Mapper mapper;

    @Before
    public void setup() {
        Article article = new Article();
        article.description = testHtml;
        mapper = new Mapper(article);
    }

    /**
     * Not a test, more like a demonstration of how rss description cleaned
     */
    @Test
    public void cleanText() {
        assertEquals(expected, mapper.getPureText());
    }

    @Test
    public void imageExtracted() {
        assertEquals("http://www.grind.fm/files/grind.fm/wlgaswl_0.png", mapper.getImageUrl());
    }
}